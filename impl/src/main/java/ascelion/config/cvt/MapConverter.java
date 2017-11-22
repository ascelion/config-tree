
package ascelion.config.cvt;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

final class MapConverter<T> implements ConfigConverter<Map<String, T>>
{

	private final Type type;
	private final ConfigConverter<T> conv;

	MapConverter( Type type, ConfigConverter<T> conv )
	{
		this.type = type;
		this.conv = conv;
	}

	@Override
	public Map<String, T> create( Type t, ConfigNode node, int unwrap )
	{
		final Map<String, T> m = new TreeMap<>();

		node.asMap( unwrap ).forEach( ( k, v ) -> m.put( k, this.conv.create( this.type, v ) ) );

		return m;
	}

	@Override
	public Map<String, T> create( Type t, String u, int unwrap )
	{
		throw new UnsupportedOperationException();
	}
}
