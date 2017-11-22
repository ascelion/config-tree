
package ascelion.config.cvt;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static ascelion.config.impl.Utils.unwrap;

final class MapConverter<T> implements ConfigConverter<Map<String, T>>
{

	private final Type type;
	private final Converters conv;

	MapConverter( Type type, Converters conv )
	{
		this.type = type;
		this.conv = conv;
	}

	@Override
	public Map<String, T> create( Type t, ConfigNode node, int unwrap )
	{
		final Map<String, T> m = new TreeMap<>();

		switch( node.getKind() ) {
			case NODE:
				final Collection<ConfigNode> nodes = node.getValue();

				nodes.forEach( n -> {
					final String p = unwrap( n.getPath(), unwrap );

					m.put( p, this.conv.getValue( this.type, n, 0 ) );
				} );
			break;

			default:
				throw new UnsupportedOperationException();
		}

		return m;
	}

	@Override
	public Map<String, T> create( Type t, String u, int unwrap )
	{
		throw new UnsupportedOperationException();
	}
}
