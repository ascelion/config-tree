
package ascelion.cdi.conf;

import java.util.List;

import ascelion.shared.cdi.conf.ConfigNode;

import static java.lang.String.format;

public final class Expression
{

	private final Expression parent;
	private List<ExpressionItem> items;
	private final String value;

	public Expression( String value )
	{
		this( null, value );
	}

	private Expression( Expression parent, String value )
	{
		this.parent = parent;
		this.value = value;
	}

	public String asItem( ConfigNode root )
	{
		return asItem( this.value, root );
	}

	public ConfigNode asNode( ConfigNode root )
	{
		return asNode( this.value, root );
	}

	private ConfigNode asNode( String val, ConfigNode root )
	{
		this.items = ExpressionItem.items( val );

		if( this.items == null ) {
			return root.getNode( val );
		}

		if( this.parent != null ) {
			this.items.forEach( i -> {
				if( this.parent.exists( i ) ) {
					throw new IllegalArgumentException( format( "Recursive definition: %s", this.value ) );
				}
			} );
		}

		return asNode( asItem( val, root ), root );
	}

	private String asItem( String val, ConfigNode root )
	{
		this.items = ExpressionItem.items( val );

		if( this.items == null ) {
			val = getItem( root );

			this.items = ExpressionItem.items( val );
		}
		if( this.items == null ) {
			return val;
		}

		if( this.parent != null ) {
			this.items.forEach( i -> {
				if( this.parent.exists( i ) ) {
					throw new IllegalArgumentException( format( "Recursive definition: %s", this.value ) );
				}
			} );
		}

		final StringBuilder b = new StringBuilder( val );

		int o = 0;

		for( final ExpressionItem i : this.items ) {
			final Expression x = new Expression( this, i.v );
			final String n = x.asItem( root );

			if( n != null ) {
				b.replace( o + i.s, o + i.e, n );

				o += n.length() - ( i.e - i.s );
			}
		}

		return b.toString();
	}

	private String getItem( ConfigNode root )
	{
		final String[] vec = this.value.split( ":" );
		String val = root.getValue( vec[0] );

		if( val == null ) {
			val = System.getProperty( vec[0] );
		}
		if( val == null && vec.length > 1 ) {
			val = new Expression( this, vec[1] ).asItem( root );
		}

		return val;
	}

	private boolean exists( ExpressionItem i )
	{
		if( this.items != null && this.items.contains( i ) ) {
			return true;
		}
		if( this.parent != null ) {
			return this.parent.exists( i );
		}
		return false;
	}

}
