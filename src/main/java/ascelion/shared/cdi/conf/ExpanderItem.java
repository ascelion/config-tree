
package ascelion.shared.cdi.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class ExpanderItem
{

	static List<ExpanderItem> items( String name )
	{
		final List<ExpanderItem> items = new ArrayList<>();

		if( isNotBlank( name ) ) {
			int s = 0;
			int e = 0;

			while( s >= 0 ) {
				s = name.indexOf( "${", e );

				if( s < 0 ) {
					break;
				}

				e = name.indexOf( "}", s );

				if( e < 0 ) {
					throw new IllegalArgumentException( format( "Invalid property value: '%s'", name ) );
				}

				final ExpanderItem i = new ExpanderItem( name, s, ++e );

				items.add( i );
			}
		}

		return unmodifiableList( items );
	}

	final int s;
	final int e;
	final String v;

	ExpanderItem( String v, int s, int e )
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
		final ExpanderItem that = (ExpanderItem) obj;

		return Objects.equals( this.v, that.v );
	}
}
