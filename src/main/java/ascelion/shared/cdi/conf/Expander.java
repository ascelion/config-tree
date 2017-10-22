
package ascelion.shared.cdi.conf;

import java.util.List;
import java.util.function.UnaryOperator;

import static java.lang.String.format;

final class Expander
{

	private final UnaryOperator<String> prop;
	private final String value;
	private final List<ExpanderItem> items;
	private final Expander parent;

	public Expander( String value, UnaryOperator<String> prop )
	{
		this( null, value, prop );
	}

	Expander( Expander parent, String value, UnaryOperator<String> prop )
	{
		this.parent = parent;
		this.prop = prop;
		this.value = value;
		this.items = ExpanderItem.items( value );

		if( parent != null ) {
			this.items.forEach( i -> {
				if( parent.exists( i ) ) {
					throw new IllegalArgumentException( format( "Recursive definition: %s", this.value ) );
				}
			} );
		}
	}

	public String expand()
	{
		if( this.value == null ) {
			return null;
		}

		final StringBuilder b = new StringBuilder( this.value );

		int o = 0;

		for( final ExpanderItem i : this.items ) {
			final String s = this.prop.apply( i.v );
			final Expander x = new Expander( this, s, this.prop );
			final String n = x.expand();

			if( n != null ) {
				b.replace( o + i.s, o + i.e, n );

				o += n.length() - ( i.e - i.s );
			}
		}

		return b.toString();
	}

	private boolean exists( ExpanderItem i )
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
