
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.List;

import ascelion.config.api.ConfigParsePosition;

import static java.lang.String.format;

@Deprecated
final class ItemTokenizerOLD
{

	private final char[] content;
	private final StringBuilder sb = new StringBuilder();
	private final List<ConfigParsePosition> errors = new ArrayList<>();

	private int offset;
	private boolean escape;

	ItemTokenizerOLD( String content )
	{
		this.content = content.toCharArray();
	}

	Token next()
	{
		if( this.sb.length() > 0 ) {
			final String tx = this.sb.toString();

			switch( tx ) {
				case Token.S_BEG:
					return new Token( this.offset, Token.Type.BEG, this.sb );
				case Token.S_DEF:
					return new Token( this.offset, Token.Type.DEF, this.sb );
				case Token.S_END:
					return new Token( this.offset, Token.Type.END, this.sb );
			}
		}

		while( this.offset < this.content.length ) {
			final char c = this.content[this.offset++];

			switch( c ) {
				case Token.C_ESC:
					if( this.escape ) {
						this.escape = false;
					}
					else {
						this.escape = true;
						this.sb.append( c );

						continue;
					}

				default:
					this.sb.append( c );
			}

			final String tx = this.sb.toString();

			if( tx.endsWith( Token.S_BEG ) && !this.escape ) {
				if( this.sb.length() > 2 ) {
					check( this.sb.length() - 2 );

					return new Token( this.offset, Token.Type.STR, this.sb, this.sb.length() - 2 );
				}
				else {
					return new Token( this.offset, Token.Type.BEG, this.sb );
				}
			}
			if( tx.endsWith( Token.S_DEF ) && !this.escape ) {
				if( this.sb.length() > 1 ) {
					check( this.sb.length() - 1 );

					return new Token( this.offset, Token.Type.STR, this.sb, this.sb.length() - 1 );
				}
				else {
					return new Token( this.offset, Token.Type.DEF, this.sb );
				}
			}
			if( tx.endsWith( Token.S_END ) && !this.escape ) {
				if( this.sb.length() > 1 ) {
					check( this.sb.length() - 1 );

					return new Token( this.offset, Token.Type.STR, this.sb, this.sb.length() - 1 );
				}
				else {
					return new Token( this.offset, Token.Type.END, this.sb );
				}
			}

			this.escape = false;
		}

		if( this.sb.length() == 0 ) {
			return null;
		}

		check( this.sb.length() );

		return new Token( this.offset, Token.Type.STR, this.sb );
	}

	public List<ConfigParsePosition> getErrors()
	{
		return this.errors;
	}

	private void check( int size )
	{
		for( int k = 0; k < size; k++ ) {
			final char c = this.sb.charAt( k );

			if( k > 0 && c == '$' && this.sb.charAt( k - 1 ) == '\\' ) {
				continue;
			}
			if( c == '$' || c == '{' ) {
				this.errors.add( new ConfigParsePosition( format( "unknown char '%c' (\\u%04d)", c, (int) c ), k ) );
			}
		}
	}
}
