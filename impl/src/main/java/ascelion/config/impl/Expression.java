
package ascelion.config.impl;

import java.util.function.UnaryOperator;

import ascelion.config.api.ConfigParseException;
import ascelion.config.api.ConfigParsePosition;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import lombok.ToString;

public class Expression
{

	@ToString
	static class Result
	{

		final String tok;
		final String val;

		Result( String tok, String val )
		{
			this.tok = tok;
			this.val = val;
		}
	}

	enum ContextName
	{
		COLLECT,
		DOLLAR,
		VALUE,
		DEFAULT,
	}

	@ToString( of = { "name", "text" } )
	static class Context
	{

		final Context prev;
		final ContextName name;
		StringBuilder text = null;
		boolean skipEval;
		int braces;

		Context( Context prev, ContextName name )
		{
			this.prev = prev;
			this.name = name;
		}

		void add( char c )
		{
			if( this.text == null ) {
				this.text = new StringBuilder();
			}
			this.text.append( c );
		}

		void add( String s )
		{
			if( this.text == null ) {
				this.text = new StringBuilder();
			}
			this.text.append( s );
		}

		String text()
		{
			return this.text != null ? this.text.toString() : null;
		}

		void skipEval()
		{
			this.skipEval = true;
		}
	}

	private final char[] content;
	private int offset;
	private Context context;
	private boolean escape;

	Expression( String content )
	{
		this.content = content.toCharArray();
	}

	String eval( UnaryOperator<String> fun )
	{
		ConfigLoopException.push( new String( this.content ) );

		try {
			return doEval( fun );
		}
		finally {
			ConfigLoopException.pop();
		}
	}

	private String doEval( UnaryOperator<String> fun )
	{
		move( ContextName.COLLECT );

		while( this.offset < this.content.length ) {
			final char ch = this.content[this.offset++];

			if( this.escape ) {
				this.escape = false;

				this.context.add( ch );

				continue;
			}
			else if( ch == '\\' ) {
				this.escape = true;

				continue;
			}

			switch( this.context.name ) {
				case COLLECT:
					switch( ch ) {
						case '$':
							move( ContextName.DOLLAR );
						break;

						case '{':
							this.context.braces++;
							this.context.add( ch );
						break;

						case '}':
							if( --this.context.braces < 0 ) {
								final ConfigParsePosition pos = new ConfigParsePosition( "unbalanced '}'", this.offset );

								throw new ConfigParseException( new String( this.content ), asList( pos ) );
							}

							this.context.add( ch );
						break;

						default:
							this.context.add( ch );
					}
				break;

				case DOLLAR:
					switch( ch ) {
						case '{':
							back();
							move( ContextName.VALUE );
						break;

						default:
							back();

							this.context.add( '$' );
							this.context.add( ch );
					}
				break;

				case VALUE:
					switch( ch ) {
						case '$':
							move( ContextName.DOLLAR );
						break;

						case '}': {
							final String tok = back().text();
							final Result res = evaluate( tok, fun, fun );

							if( res.val == null && this.context.name != ContextName.VALUE ) {
								throwUnresolved( res.tok );
							}

							if( res.val != null ) {
								this.context.add( res.val );
							}
						}
						break;

						case ':': {
							final String tok = this.context.text();
							final Result res = evaluate( tok, fun, fun );

							back();

							if( res.val == null ) {
								move( ContextName.DEFAULT );
							}
							else {
								this.context.add( res.val );

								move( ContextName.DEFAULT );

								this.context.skipEval();
							}
						}
						break;

						default:
							this.context.add( ch );
					}
				break;

				case DEFAULT: {
					switch( ch ) {
						case '$':
							move( ContextName.DOLLAR );
						break;

						case '}':
							if( this.context.skipEval ) {
								back();
							}
							else {
								final String tok = this.context.text();
								final Result res = evaluate( tok, tok, fun );

								if( res == null ) {
									throwUnresolved( this.context.text() );
								}

								back();

								this.context.add( res.val );
							}
						break;

						default:
							this.context.add( ch );
					}
				}
				break;
			}
		}

		if( this.context.prev != null ) {
			final ConfigParsePosition pos = new ConfigParsePosition( "expression error", this.offset );

			throw new ConfigParseException( new String( this.content ), asList( pos ) );
		}

		return back().text();
	}

	private void move( ContextName name )
	{
		this.context = new Context( this.context, name );
	}

	private Context back()
	{
		if( this.context == null ) {
			final ConfigParsePosition pos = new ConfigParsePosition( "expression error", this.offset );

			throw new ConfigParseException( new String( this.content ), asList( pos ) );
		}
		if( this.context.braces > 0 ) {
			final ConfigParsePosition pos = new ConfigParsePosition( "unbalanced '{'", this.offset );

			throw new ConfigParseException( new String( this.content ), asList( pos ) );
		}

		final Context cx = this.context;

		this.context = cx.prev;

		return cx;
	}

	private void throwUnresolved( String tok )
	{
		final ConfigParsePosition pos = new ConfigParsePosition( format( "unresolved '%s'", tok ), this.offset );

		throw new ConfigParseException( new String( this.content ), asList( pos ) );
	}

	private Result evaluate( String tok, UnaryOperator<String> first, UnaryOperator<String> next )
	{
		if( this.context.skipEval ) {
			return new Result( tok, tok );
		}

		return evaluate( tok, first.apply( tok ), next );
	}

	private Result evaluate( String tok, String val, UnaryOperator<String> fun )
	{
		if( val == null ) {
			return new Result( tok, val );
		}

		val = new Expression( val ).eval( fun );

		return new Result( tok, val );
	}
}
