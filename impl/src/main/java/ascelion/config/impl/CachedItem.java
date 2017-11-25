
package ascelion.config.impl;

import java.util.Map;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNode.Kind;

final class CachedItem
{

	private final ConfigNodeImpl node;
	private Kind kind;
	private final Object value;
	private Object cached = null;
	private final boolean def;

	CachedItem( ConfigNodeImpl node )
	{
		this.node = node;
		this.kind = Kind.NULL;
		this.value = null;
		this.def = false;
	}

	CachedItem( Object item, ConfigNodeImpl node )
	{
		this( item, node, false );
	}

	CachedItem( Object item, ConfigNodeImpl node, boolean def )
	{
		this.node = node;

		if( item == null ) {
			this.kind = Kind.NULL;
		}
		else if( item instanceof Expression ) {
			this.kind = Kind.ITEM;
		}
		else if( item instanceof String ) {
			this.kind = Kind.ITEM;
			this.cached = item;
		}
		else if( item instanceof Map ) {
			this.kind = Kind.NODE;
			this.cached = item;
		}
		else {
			throw new IllegalArgumentException( "Unsuported type: " + item.getClass().getName() );
		}

		this.value = item;
		this.def = def;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		switch( this.kind ) {
			case NULL:
				sb.append( "<NULL>" );
			break;

			case LINK:
			case ITEM:
				sb.append( this.value.toString() );
			break;

			case NODE:
				sb.append( "<NODE>" );
			break;
		}

		return sb.toString();
	}

	Kind kindNoEval()
	{
		return this.kind;
	}

	ConfigNode.Kind kind()
	{
		cached();

		return this.kind;
	}

	boolean isDefault()
	{
		return this.def;
	}

	ConfigNodeImpl node()
	{
		return this.node;
	}

	<T> T value()
	{
		return (T) this.value;
	}

	<T> T cached()
	{
		switch( this.kind ) {
			case NULL:
				return null;

			case ITEM:
				if( this.cached != null ) {
					return (T) this.cached;
				}

				final CachedItem val = ( (Expression) this.value ).eval( this.node );

				if( val.kind != Kind.NODE ) {
					this.kind = val.kind;
					this.cached = val.cached();

					return (T) this.cached;
				}

				this.kind = Kind.LINK;
				this.cached = val;

			case LINK:
				return ( (CachedItem) this.cached ).cached();

			case NODE:
				return (T) this.cached;
		}

		throw new AssertionError( "UNREACHABLE CODE" );
	}
}
