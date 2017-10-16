
package ascelion.shared.cdi.conf;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

class ConfigMap
{

	private final Supplier<Map<String, Object>> prov;
	private final Map<String, Object> root;

	ConfigMap()
	{
		this( TreeMap::new );
	}

	ConfigMap( Supplier<Map<String, Object>> prov )
	{
		this.prov = prov;
		this.root = prov.get();
	}

	public Map<String, Object> get()
	{
		return this.root;
	}

	public void put( Map<String, Object> root )
	{
		this.root.putAll( root );
	}

	public void add( Map<String, Object> root )
	{
		merge( this.root, root );
	}

	Object getValue( String key )
	{
		final String[] keys = key.split( "\\." );

		return getValue( this.root, keys, 0 );
	}

	void setValue( String key, String val )
	{
		final String[] keys = key.split( "\\." );

		setValue( this.root, keys, 0, val );
	}

	private void merge( Map<String, Object> m1, Map<String, Object> m2 )
	{
		m2.forEach( ( k, v ) -> {
			final Object t = m1.get( k );

			if( v instanceof Map && t instanceof Map ) {
				merge( (Map) t, (Map) v );
			}
			else {
				m1.put( k, v );
			}
		} );
	}

	private Object getValue( Map<String, Object> map, String[] keys, int depth )
	{
		final Object val = map.get( keys[depth] );

		if( val == null ) {
			return null;
		}

		if( depth == keys.length - 1 ) {
			return val;
		}
		if( val instanceof Map ) {
			return getValue( (Map<String, Object>) val, keys, depth + 1 );
		}
		else {
			return null;
		}
	}

	private void setValue( Map<String, Object> map, String[] keys, int depth, Object value )
	{
		if( depth == keys.length - 1 ) {
			map.put( keys[depth], value );
		}
		else {
			final Object val = map.get( keys[depth] );

			if( val instanceof Map ) {
				map = (Map) val;
			}
			else {
				final Map<String, Object> cld = this.prov.get();

				map.put( keys[depth], cld );

				map = cld;
			}

			setValue( map, keys, depth + 1, value );
		}
	}
}
