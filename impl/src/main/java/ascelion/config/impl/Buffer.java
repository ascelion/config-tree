
package ascelion.config.impl;

import java.util.Arrays;

final class Buffer
{

	private char[] content;
	private final int offset;
	private int count;

	Buffer( String content )
	{
		this( content.toCharArray() );
	}

	Buffer( char[] content )
	{
		this( content, 0, content.length );
	}

	Buffer( char[] content, int offset, int count )
	{
		this.content = content;
		this.offset = offset;
		this.count = count;
	}

	@Override
	public String toString()
	{
		return new String( this.content, this.offset, this.count );
	}

	char[] chars()
	{
		return this.content;
	}

	int offset()
	{
		return this.offset;
	}

	int count()
	{
		return this.count;
	}

	int find( String text )
	{
		return find( text, this.offset, this.count );
	}

	int find( String text, int offset, int count )
	{
		return find( text.toCharArray(), offset, count );
	}

	int find( final char[] c, int offset, int count )
	{
		if( c.length == 0 ) {
			return offset;
		}

		for( int o = offset; o < count; o++ ) {
			if( matches( c, o, count ) ) {
				return o;
			}
		}

		return -1;
	}

	int find( final char[] c, char escape )
	{
		return find( c, this.offset, this.count, escape, this.offset );
	}

	int find( final char[] c, int offset, int count, char escape, int start )
	{
		if( c.length == 0 ) {
			return offset;
		}

		for( int o = offset; o < count; o++ ) {
			if( matches( c, o, count, escape, start ) ) {
				return o;
			}
		}

		return -1;
	}

	boolean matches( String text )
	{
		return matches( text.toCharArray(), this.offset, this.count );
	}

	boolean matches( String text, int offset, int count )
	{
		return matches( text.toCharArray(), offset, count );
	}

	boolean matches( char[] c, int offset, int count )
	{
		if( c.length == 0 ) {
			return true;
		}
		if( c.length + offset > count ) {
			return false;
		}

		for( int k = 0; k < c.length; k++ ) {
			if( c[k] != this.content[offset + k] ) {
				return false;
			}
		}

		return true;
	}

	boolean startsWith( String prefix )
	{
		final char[] c = prefix.toCharArray();

		if( c.length > this.count ) {
			return false;
		}

		for( int k = 0; k < c.length; k++ ) {
			if( this.content[this.offset + k] != c[k] ) {
				return false;
			}
		}

		return true;
	}

	int delete( int offset )
	{
		return delete( offset, 1 );
	}

	int delete( int offset, int count )
	{
		this.count -= count;

		System.arraycopy( this.content, offset + count, this.content, offset, this.count - offset );

		return count;
	}

	int replace( int offset, int count, String text )
	{
		return -delete( offset, count ) + insert( offset, text );
	}

	int insert( int offset, String text )
	{
		if( text != null ) {
			final int z = text.length();

			if( z > 0 ) {
				final int newZ = this.count + z;

				if( newZ > this.content.length ) {
					this.content = Arrays.copyOf( this.content, newZ );
				}

				System.arraycopy( this.content, offset, this.content, offset + z, newZ - offset - z );
				System.arraycopy( text.toCharArray(), 0, this.content, offset, z );

				this.count = newZ;
			}

			return z;
		}

		return 0;
	}

	char at( int k )
	{
		return this.content[k];
	}

	Buffer subBuffer( int offset, int count )
	{
		return new Buffer( new String( this.content, offset, count ) );
	}

	boolean matches( char[] prefix, int offset, int count, char escape, int start )
	{
		if( offset > start && this.content[offset - 1] == escape ) {
			return false;
		}

		return matches( prefix, offset, count );
	}

}
