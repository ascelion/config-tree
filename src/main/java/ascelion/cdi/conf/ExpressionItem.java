
package ascelion.cdi.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class ExpressionItem
{

	static List<ExpressionItem> items( String value )
	{
		final List<ExpressionItem> items = new ArrayList<>();

		if( isNotBlank( value ) ) {
			int s = 0;

			ExpressionItem i;

			while( ( i = nextItem( value, s ) ) != null ) {
				items.add( i );

				s = i.e + 1;
			}
		}

		return items.isEmpty() ? null : unmodifiableList( items );
	}

	static ExpressionItem nextItem( String v, int o )
	{
		final int z = v.length();
		int n = 0;
		int s = 0;
		int e = 0;

		for( int x = o; x < z; x++ ) {
			final char c = v.charAt( x );

			switch( c ) {
				case ':': {
					if( n == 0 ) {
						e = x + 1;
					}
				}
				break;

				case '$': {
					if( n++ == 0 ) {
						s = x;
					}

					x++;

					if( x == z || x < z && v.charAt( x ) != '{' ) {
						throw new IllegalArgumentException( format( "Invalid property value: '%s'", v ) );
					}
				}
				break;

				case '}': {
					if( --n < 0 ) {
						throw new IllegalArgumentException( format( "Invalid property value: '%s'", v ) );
					}
					if( n == 0 ) {
						return new ExpressionItem( v, s, e > 0 ? e : x + 1 );
					}
				}
				break;
			}
		}

		if( n > 0 ) {
			throw new IllegalArgumentException( format( "Invalid property value: '%s'", v ) );
		}

		return null;
	}

	final int s;
	final int e;
	final String v;

	ExpressionItem( String v, int s, int e )
	{
		this.s = s;
		this.e = e;
		this.v = v.substring( s + 2, e - 1 );
	}

	@Override
	public String toString()
	{
		return format( "%s[%d,%d]", this.v, this.s, this.e );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( this.v );
	}

	@Override
	public boolean equals( Object obj )
	{
		if( this == obj ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}
		final ExpressionItem that = (ExpressionItem) obj;

		return Objects.equals( this.v, that.v );
	}
}
