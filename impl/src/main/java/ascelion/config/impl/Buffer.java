
package ascelion.config.impl;

import java.util.Arrays;

import static java.util.Arrays.copyOfRange;

final class Buffer
{

	char[] content;
	final int offset;
	int count;

	Buffer( String content )
	{
		this( content.toCharArray(), 0, content.length() );
	}

	private Buffer( char[] content, int offset, int count )
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

	int find( final char[] c, char escape )
	{
		if( c.length == 0 ) {
			return this.offset;
		}

		for( int o = this.offset; o < this.offset + this.count; o++ ) {
			if( matches( c, o, escape ) ) {
				return o;
			}
		}

		return -1;
	}

	boolean matches( char[] prefix, int offset, char escape )
	{
		if( offset > this.offset && this.content[offset - 1] == escape ) {
			return false;
		}

		if( prefix.length == 0 ) {
			return true;
		}
		if( prefix.length + offset > this.offset + this.count ) {
			return false;
		}

		for( int k = 0; k < prefix.length; k++ ) {
			if( prefix[k] != this.content[offset + k] ) {
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

	Buffer subBuffer( int offset, int count )
	{
		return new Buffer( this.content, offset, count );
	}

	Buffer newBuffer( int offset, int count )
	{
		return new Buffer( copyOfRange( this.content, offset, offset + count ), 0, count );
	}

	String toString( int offset, int count )
	{
		return new String( this.content, offset, count );
	}
}
