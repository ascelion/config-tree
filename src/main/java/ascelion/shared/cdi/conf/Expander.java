
package ascelion.shared.cdi.conf;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import static java.lang.String.format;

import org.jboss.weld.exceptions.IllegalArgumentException;

final class Expander
{

	static private final Expander NULL = new Expander( null, x -> x );

	static class Item
	{

		final int s;
		final int e;
		final String v;

		Item( String v, int s, int e )
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
			final Item that = (Item) obj;

			return Objects.equals( this.v, that.v );
		}
	}

	private final UnaryOperator<String> prop;
	private final String value;
	private final Set<Item> items = new LinkedHashSet<>();
	private final Expander parent;

	Expander( String value, UnaryOperator<String> prop )
	{
		this( null, value, prop );
	}

	Expander( Expander parent, String value, UnaryOperator<String> prop )
	{
		this.parent = parent;
		this.prop = prop;
		this.value = prop.apply( value );

		if( this.value != null ) {
			int s = 0;
			int e = 0;

			while( s >= 0 ) {
				s = this.value.indexOf( "${", e );

				if( s < 0 ) {
					break;
				}

				e = this.value.indexOf( "}", s );

				if( e < 0 ) {
					throw new IllegalArgumentException( format( "Invalid property value: '%s'", value ) );
				}

				final Item i = new Item( this.value, s, ++e );

				if( parent != null && parent.exists( i ) ) {
					throw new IllegalArgumentException( format( "Recursive definition: %s", this.value ) );
				}

				this.items.add( i );
			}
		}
	}

	public String expand()
	{
		if( this.value == null ) {
			return null;
		}

		final StringBuilder b = new StringBuilder( this.value );

		int o = 0;

		for( final Item i : this.items ) {
			final Expander x = new Expander( this, i.v, this.prop );
			final String n = x.expand();

			if( n != null ) {
				b.replace( o + i.s, o + i.e, n );

				o = n.length() - i.e + i.s;
			}
		}

		return b.toString();
	}

	private boolean exists( Item i )
	{
		if( this.items.contains( i ) ) {
			return true;
		}
		if( this.parent != null ) {
			return this.parent.exists( i );
		}
		return false;
	}
}
