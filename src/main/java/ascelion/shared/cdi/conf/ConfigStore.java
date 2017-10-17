
package ascelion.shared.cdi.conf;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import org.jboss.weld.exceptions.IllegalArgumentException;

public class ConfigStore
{

	private final Supplier<Map<String, Object>> prov;
	private final Map<String, Object> root;

	public ConfigStore()
	{
		this( TreeMap::new );
	}

	public ConfigStore( Supplier<Map<String, Object>> prov )
	{
		this.prov = prov;
		this.root = prov.get();
	}

	public Map<String, Object> get()
	{
		return this.root;
	}

	public void add( Map<String, Object> root )
	{
		merge( this.root, root );
	}

	public Object getValue( String key )
	{
		final String[] keys = key.split( "\\." );

		return getValue( this.root, keys, 0 );
	}

	public void setValue( String key, String val )
	{
		final String[] keys = key.split( "\\." );

		setValue( this.root, keys, 0, val );
	}

	private void merge( Map<String, Object> m1, Map<String, Object> m2 )
	{
		m2.forEach( ( k, v ) -> {
			Object t = m1.get( k );

			if( v instanceof Map ) {
				if( t == null ) {
					t = this.prov.get();

					m1.put( k, t );
				}
				else if( !( t instanceof Map ) ) {
					throw new IllegalStateException( "property mismatch" );
				}

				merge( (Map) t, (Map) v );
			}
			else if( v != null ) {
				if( t instanceof Map ) {
					throw new IllegalStateException( "property mismatch" );
				}

				m1.put( k, v.toString() );
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
		final Object val = map.get( keys[depth] );

		if( depth == keys.length - 1 ) {
			if( val instanceof Map ) {
				throw new IllegalArgumentException( format( "There is alread a map at path %s", asList( keys ) ) );
			}

			map.put( keys[depth], value );
		}
		else {
			if( val instanceof Map ) {
				map = (Map) val;
			}
			else if( val != null ) {
				throw new IllegalArgumentException( format( "There is alread a value at path %s", asList( keys ).subList( 0, depth + 1 ) ) );
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
