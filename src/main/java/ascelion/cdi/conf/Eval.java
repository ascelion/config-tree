
package ascelion.cdi.conf;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

import com.google.common.base.Objects;

final class Eval
{

	static class Token
	{

		static final char C_ESC = '\\';
		static final String S_BEG = "${";
		static final String S_DEF = ":";
		static final String S_END = "}";

		enum Type
		{
			EOF,
			BEG,
			DEF,
			END,
			STR,
		}

		final Type type;
		final String text;

		Token( Type type, StringBuilder b )
		{
			this( type, b, b.length() );
		}

		Token( Type type, StringBuilder b, int count )
		{
			this.type = type;
			this.text = b.substring( 0, count ).toString();

			b.delete( 0, count );
		}
	}

	static class Tokenizer
	{

		final Reader rd;
		final StringBuilder sb = new StringBuilder();
		final List<EvalError> errors = new ArrayList<>();

		int position;
		private boolean escape;

		Tokenizer( Reader rd )
		{
			this.rd = rd;
		}

		Token next()
		{
			if( this.sb.length() > 0 ) {
				final String tx = this.sb.toString();

				switch( tx ) {
					case Token.S_BEG:
						return new Token( Token.Type.BEG, this.sb );
					case Token.S_DEF:
						return new Token( Token.Type.DEF, this.sb );
					case Token.S_END:
						return new Token( Token.Type.END, this.sb );
				}
			}

			while( true ) {
				final char c;

				try {
					final int n = this.rd.read();
					if( n == -1 ) {
						break;
					}

					c = (char) n;
				}
				catch( final IOException e ) {
					throw new EvalException( e.getMessage() );
				}

				this.position++;

				switch( c ) {
					case Token.C_ESC:
						if( this.escape ) {
							this.escape = false;
						}
						else {
							this.escape = true;

							continue;
						}

					default:
						this.sb.append( c );
				}

				final String tx = this.sb.toString();

				if( tx.endsWith( Token.S_BEG ) && !this.escape ) {
					if( this.sb.length() > 2 ) {
						check( this.sb.length() - 2 );

						return new Token( Token.Type.STR, this.sb, this.sb.length() - 2 );
					}
					else {
						return new Token( Token.Type.BEG, this.sb );
					}
				}
				if( tx.endsWith( Token.S_DEF ) && !this.escape ) {
					if( this.sb.length() > 1 ) {
						check( this.sb.length() - 1 );

						return new Token( Token.Type.STR, this.sb, this.sb.length() - 1 );
					}
					else {
						return new Token( Token.Type.DEF, this.sb );
					}
				}
				if( tx.endsWith( Token.S_END ) && !this.escape ) {
					if( this.sb.length() > 1 ) {
						check( this.sb.length() - 1 );

						return new Token( Token.Type.STR, this.sb, this.sb.length() - 1 );
					}
					else {
						return new Token( Token.Type.END, this.sb );
					}
				}

				this.escape = false;
			}

			if( this.sb.length() == 0 ) {
				return new Token( Token.Type.EOF, this.sb );
			}

			check( this.sb.length() );

			return new Token( Token.Type.STR, this.sb );
		}

		void check( int size )
		{
			for( int k = 0; k < size; k++ ) {
				final char c = this.sb.charAt( k );

				if( c == '$' || c == '{' ) {
					this.errors.add( new EvalError( format( "unknown char '%c' (\\u%04d)", c, (int) c ), k ) );
				}
			}
		}
	}

	static abstract class Rule
	{

		final List<Rule> children = new ArrayList<>();
		Rule parent;
		Rule base;

		<T extends Rule> T push( T child )
		{
			child.parent = child.base = this;

			this.children.add( child );

			return child;
		}

		<T extends Rule> T pop()
		{
			return (T) this.parent;
		}

		abstract String eval( ConfigNode root, Function<String, Rule> parser );

		@Override
		public String toString()
		{
			return this.children.stream().map( Rule::toString ).collect( Collectors.joining() );
		}

		@Override
		public boolean equals( Object obj )
		{
			if( obj == this ) {
				return true;
			}
			if( obj == null ) {
				return false;
			}

			if( getClass() != obj.getClass() ) {
				return false;
			}

			final Rule that = (Rule) obj;

			return Objects.equal( this.children, that.children );
		}
	}

	static class Text extends Rule
	{

		private final String text;

		Text( String text )
		{
			this.text = text;
		}

		@Override
		String eval( ConfigNode root, Function<String, Rule> parser )
		{
			return this.text;
		}

		@Override
		public String toString()
		{
			return this.text;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( obj == this ) {
				return true;
			}
			if( obj == null ) {
				return false;
			}

			if( getClass() != obj.getClass() ) {
				return false;
			}

			final Text that = (Text) obj;

			return Objects.equal( this.text, that.text );
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode( this.text );
		}
	}

	static class Expr extends Rule
	{

		final List<Rule> val = new ArrayList<>();
		final List<Rule> def = new ArrayList<>();
		boolean right;

		void toDefault()
		{
			this.right = true;
		}

		@Override
		<T extends Rule> T push( T child )
		{
			super.push( child );

			if( this.right ) {
				this.def.add( child );
			}
			else {
				this.val.add( child );
			}

			return child;
		}

		@Override
		String eval( ConfigNode root, Function<String, Rule> parser )
		{
			final StringBuilder b = new StringBuilder();
			b.insert( 0, this );

			for( Rule ex = this.base; ex != null; ex = ex.base ) {
				b.insert( 0, "->" );
				b.insert( 0, ex );

				if( Objects.equal( this, ex ) ) {
					throw new EvalException( format( "recursive definition: %s", b ) );
				}
			}

			final String path = eval( this.val, root, parser );

			if( path == null ) {
				return null;
			}

			String item = getValue( root, path );

			if( item == null ) {
				item = eval( this.def, root, parser );
			}
			if( item == null ) {
				item = System.getProperty( path );
			}
			if( item == null ) {
				return null;
			}

			if( item.contains( "${" ) ) {
				final Rule expr = parser.apply( item );

				expr.base = this;

				return expr.eval( root, parser );
			}
			else {
				return item;
			}
		}

		String getValue( ConfigNode root, final String prop )
		{
			if( this.parent != null || this.parent == null && this.val.stream().allMatch( Text.class::isInstance ) ) {
				return root.getValue( prop );
			}

			return prop;
		}

		final String eval( List<Rule> rules, ConfigNode root, Function<String, Rule> parser )
		{
			switch( rules.size() ) {
				case 0:
					return null;
				case 1:
					return rules.get( 0 ).eval( root, parser );

				default:
					return rules.stream().map( rule -> rule.eval( root, parser ) ).collect( Collectors.joining() );
			}
		}

		@Override
		public String toString()
		{
			final StringBuilder b = new StringBuilder();

			this.val.forEach( b::append );

			if( this.def.size() > 0 ) {
				b.append( Token.S_DEF );

				this.def.forEach( b::append );
			}

			if( this.parent != null || this.parent == null && this.val.stream().allMatch( Text.class::isInstance ) ) {
				return format( "%s%s%s", Token.S_BEG, b, Token.S_END );
			}
			else {
				return b.toString();
			}
		}
	}

	static Rule parse( String content )
	{
		final StringReader rd = new StringReader( content );
		final Tokenizer tkz = new Tokenizer( rd );
		Token tk = null;
		boolean eof = false;
		Expr root = new Expr();

		while( !eof ) {
			tk = tkz.next();

			switch( tk.type ) {
				case EOF:
					eof = true;
				break;

				case BEG:
					root = root.push( new Expr() );
				break;

				case END:
					if( root.parent == null ) {
						tkz.errors.add( new EvalError( "unbalanced '}'", tkz.position ) );

						eof = true;

						break;
					}

					root = root.pop();
				break;

				case DEF:
					root.toDefault();
				break;

				case STR:
					root.push( new Text( tk.text ) );
				break;
			}
		}
		if( root.parent != null ) {
			throw new EvalException( "unbalanced ${" );
		}
		if( tkz.errors.size() > 0 ) {
			throw new EvalException( tkz.errors );
		}
		if( root.val.stream().allMatch( Text.class::isInstance ) ) {
			final Expr expr = new Expr();

			expr.push( root );

			root = expr;
		}

		return root;
	}

	static String eval( String value, ConfigNode root )
	{
		final Rule rule = parse( value );

		return rule.eval( root, Eval::parse );
	}

}
