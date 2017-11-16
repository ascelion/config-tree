
package ascelion.config.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigParsePosition;

import static java.lang.String.format;

final class ItemTokenizer
{

	static final class Token
	{

		static final char C_ESC = '\\';
		static final String S_BEG = "${";
		static final String S_DEF = ":";
		static final String S_END = "}";

		enum Type
		{
			BEG( S_BEG ),
			DEF( S_DEF ),
			END( S_END ),
			STR,

			;

			final String value;

			Type()
			{
				this( "" );
			}

			Type( String value )
			{
				this.value = value;
			}
		}

		final int position;
		final Type type;
		final String text;

		Token( int position, Type type, StringBuilder b )
		{
			this( position, type, b, b.length() );
		}

		Token( int position, Type type, StringBuilder b, int count )
		{
			this.position = position;
			this.type = type;
			this.text = b.substring( 0, count ).toString();

			b.delete( 0, count );
		}

		@Override
		public String toString()
		{
			if( this.text.isEmpty() ) {
				return format( "%s[%d]", this.type, this.position );
			}
			else {
				return format( "%s[%d] - %s", this.type, this.position, this.text );
			}
		}
	}

	private final Reader rd;
	private final StringBuilder sb = new StringBuilder();
	private final List<ConfigParsePosition> errors = new ArrayList<>();

	private int position;
	private boolean escape;

	ItemTokenizer( Reader rd )
	{
		this.rd = rd;
	}

	Token next()
	{
		if( this.sb.length() > 0 ) {
			final String tx = this.sb.toString();

			switch( tx ) {
				case Token.S_BEG:
					return new Token( this.position, Token.Type.BEG, this.sb );
				case Token.S_DEF:
					return new Token( this.position, Token.Type.DEF, this.sb );
				case Token.S_END:
					return new Token( this.position, Token.Type.END, this.sb );
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
				throw new ConfigException( e.getMessage() );
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

					return new Token( this.position, Token.Type.STR, this.sb, this.sb.length() - 2 );
				}
				else {
					return new Token( this.position, Token.Type.BEG, this.sb );
				}
			}
			if( tx.endsWith( Token.S_DEF ) && !this.escape ) {
				if( this.sb.length() > 1 ) {
					check( this.sb.length() - 1 );

					return new Token( this.position, Token.Type.STR, this.sb, this.sb.length() - 1 );
				}
				else {
					return new Token( this.position, Token.Type.DEF, this.sb );
				}
			}
			if( tx.endsWith( Token.S_END ) && !this.escape ) {
				if( this.sb.length() > 1 ) {
					check( this.sb.length() - 1 );

					return new Token( this.position, Token.Type.STR, this.sb, this.sb.length() - 1 );
				}
				else {
					return new Token( this.position, Token.Type.END, this.sb );
				}
			}

			this.escape = false;
		}

		if( this.sb.length() == 0 ) {
			return null;
		}

		check( this.sb.length() );

		return new Token( this.position, Token.Type.STR, this.sb );
	}

	public List<ConfigParsePosition> getErrors()
	{
		return this.errors;
	}

	private void check( int size )
	{
		for( int k = 0; k < size; k++ ) {
			final char c = this.sb.charAt( k );

			if( c == '$' || c == '{' ) {
				this.errors.add( new ConfigParsePosition( format( "unknown char '%c' (\\u%04d)", c, (int) c ), k ) );
			}
		}
	}
}
