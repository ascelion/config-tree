
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import ascelion.config.api.ConfigParseException;
import ascelion.config.api.ConfigParsePosition;

final class ItemTokenizer
{

	static enum Context
	{
		COLLECT,
		ESCAPE,
		DOLLAR,
		VALUE,
		DEFAULT,
	}

	private final char[] content;
	private int offset;
	private final StringBuilder text = new StringBuilder();
	private final Deque<Context> contexts = new LinkedList<>();
	private final List<ConfigParsePosition> errors = new ArrayList<>();

	ItemTokenizer( String content )
	{
		this.content = content.toCharArray();

		push( Context.COLLECT );
	}

	Token next()
	{
		while( this.offset < this.content.length ) {
			final char c = this.content[this.offset++];

			switch( context() ) {
				case ESCAPE: {
					pop();

					this.text.append( c );
				}
				break;

				case COLLECT: {
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '$':
							push( Context.DOLLAR );
						break;

						default:
							this.text.append( c );
					}
				}
				break;

				case DOLLAR: {
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '{':
							pop();
							push( Context.VALUE );
							return new Token( this.offset, Token.Type.BEG, this.text );

						default:
							pop();
							this.text.append( '$' );
					}
				}
				break;

				case VALUE: {
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '}': {
							pop();

							return new Token( this.offset, Token.Type.END, this.text );
						}

						case ':': {
							pop();
							push( Context.DEFAULT );

							return new Token( this.offset, Token.Type.DEF, this.text );
						}

						default:
							this.text.append( c );
					}
				}
				break;

				case DEFAULT:
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '}': {
							pop();

							return new Token( this.offset, Token.Type.END, this.text );
						}

						default:
							this.text.append( c );
					}
				break;
			}
		}

		if( this.text.length() == 0 ) {
			if( this.contexts.size() > 1 ) {
				final String message;

				switch( pop() ) {
					case ESCAPE:
						message = "waiting for escaped character";
					break;

					case DOLLAR:
						this.text.append( '$' );

						return new Token( this.offset, Token.Type.STR, this.text );

					case VALUE:
						message = "waiting for closing brace or colon";
					break;

					case DEFAULT:
						message = "waiting for closing brace";
					break;

					default:
						message = "internal error: COLLECT";
				}

				this.errors.add( new ConfigParsePosition( message, this.offset ) );
			}

			return null;
		}

		return new Token( this.offset, Token.Type.STR, this.text );
	}

	public List<ConfigParsePosition> getErrors()
	{
		return this.errors;
	}

	private void push( Context context )
	{
		this.contexts.push( context );
	}

	private Context context()
	{
		return this.contexts.peek();
	}

	private Context pop()
	{
		try {
			return this.contexts.pop();
		}
		catch( final NoSuchElementException e ) {
			this.errors.add( new ConfigParsePosition( "unknown context", this.offset ) );

			throw new ConfigParseException( new String( this.content ), this.errors );
		}
	}
}
