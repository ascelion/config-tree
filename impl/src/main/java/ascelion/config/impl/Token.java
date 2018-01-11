
package ascelion.config.impl;

import static java.lang.String.format;

@Deprecated
final class Token
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

	final int offset;
	final Token.Type type;
	final String text;

	Token( int position, Token.Type type, StringBuilder b )
	{
		this( position, type, b, b.length() );
	}

	Token( int position, Token.Type type, StringBuilder b, int count )
	{
		this.offset = position;
		this.type = type;
		this.text = b.substring( 0, count ).toString().replace( "\\:", ":" );

		b.delete( 0, count );
	}

	@Override
	public String toString()
	{
		if( this.text.isEmpty() ) {
			return format( "%s[%d]", this.type, this.offset );
		}
		else {
			return format( "%s[%d] - %s", this.type, this.offset, this.text );
		}
	}
}
