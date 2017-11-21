
package ascelion.config.impl;

import java.util.Map;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNode.Kind;

final class CachedItem
{

	ConfigNodeImpl node;
	private Kind kind;
	private final Object value;
	private Object cached = null;

	CachedItem()
	{
		this.node = null;
		this.kind = Kind.NULL;
		this.value = null;
	}

	CachedItem( ConfigNodeImpl node )
	{
		this.node = node;
		this.kind = Kind.NULL;
		this.value = null;
	}

	CachedItem( Object item, ConfigNodeImpl node )
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
		else if( item instanceof ConfigNodeImpl ) {
			this.kind = Kind.LINK;
			this.cached = item;
		}
		else {
			throw new IllegalArgumentException( "Unsuported type: " + item.getClass().getName() );
		}

		this.value = item;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		switch( this.kind ) {
			case NULL:
				sb.append( "<NULL>" );
			break;

			case ITEM:
				sb.append( this.value.toString() );
			break;

			case LINK:
				sb.append( ( (ConfigNodeImpl) this.value ).path );
			break;

			case NODE:
				sb.append( "<NODE>" );
			break;
		}

		return sb.toString();
	}

	public Kind kindNoEval()
	{
		return this.kind;
	}

	ConfigNode.Kind kind()
	{
		cached();

		return this.kind;
	}

	<T> T value()
	{
		return (T) this.value;
	}

	<T> T cached()
	{
		if( this.kind == Kind.NULL || this.cached != null ) {
			return (T) this.cached;
		}

		if( this.kind == Kind.ITEM ) {
			final CachedItem val = ( (Expression) this.value ).eval( this.node );

			this.node = val.node;
			this.kind = val.kind;
			this.cached = val.cached;
		}

		return (T) this.cached;
	}
}
