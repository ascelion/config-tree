
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.impl.Utils;

import static ascelion.config.impl.Utils.unwrap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

class MapConverter<T> implements ConfigConverter<Map<String, T>>
{

	private final Type type;
	private final ConfigConverter<T> conv;

	MapConverter( Type type, ConfigConverter<T> conv )
	{
		this.type = type;
		this.conv = conv;
	}

	@Override
	public Map<String, T> create( Type t, ConfigNode u, int unwrap )
	{
		if( u == null ) {
			return emptyMap();
		}

		final Map<String, T> m = new TreeMap<>();

		if( Utils.isContainer( this.type ) ) {
			final Collection<ConfigNode> nodes = u.getNodes();

			if( nodes != null ) {
				nodes.forEach( n -> {
					m.put( unwrap( n.getPath(), unwrap ), this.conv.create( this.type, n, unwrap ) );
				} );
			}
		}
		else {
			asMap( u )
				.forEach( ( k, v ) -> {
					m.put( unwrap( k, unwrap ), this.conv.create( this.type, v ) );
				} );
		}

		return unmodifiableMap( m );
	}

	@Override
	public Map<String, T> create( Type t, String u )
	{
		if( isBlank( u ) ) {
			return emptyMap();
		}

		throw new UnsupportedOperationException();
	}

	private Map<String, String> asMap( ConfigNode node )
	{
		final Map<String, String> map = new TreeMap<>();

		fillMap( node, map );

		return map;
	}

	private void fillMap( ConfigNode node, Map<String, String> m )
	{
		if( node.getValue() != null ) {
			m.put( node.getPath(), node.getValue() );
		}

		final Collection<ConfigNode> nodes = node.getNodes();

		if( nodes != null ) {
			nodes.forEach( n -> fillMap( n, m ) );
		}
	}

}
