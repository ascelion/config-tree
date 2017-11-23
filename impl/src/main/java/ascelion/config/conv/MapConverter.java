
package ascelion.config.conv;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.impl.Utils;

import static ascelion.config.impl.Utils.unwrap;

class MapConverter<T> implements ConfigConverter<Map<String, T>>
{

	private final Type type;
	private final ConfigConverter<T> conv;

	MapConverter( ConfigConverter<T> conv )
	{
		final Type ct = Utils.paramType( getClass(), ConfigConverter.class, 0 );

		if( !( ct instanceof ParameterizedType ) ) {
			throw new IllegalArgumentException( "No type info" );
		}

		final ParameterizedType pt = (ParameterizedType) ct;

		this.type = pt.getActualTypeArguments()[0];
		this.conv = conv;
	}

	@Override
	public Map<String, T> create( Type t, ConfigNode node, int unwrap )
	{
		final Map<String, T> m = new TreeMap<>();

		switch( node.getKind() ) {
			case NODE:
				if( Utils.isContainer( this.type ) ) {
					m.put( unwrap( node.getPath(), unwrap ), this.conv.create( this.type, node, 0 ) );
				}
				else {
					asMap( node ).forEach( ( k, v ) -> {
						m.put( unwrap( k, unwrap ), this.conv.create( this.type, v ) );
					} );
				}
			break;

			default:
				throw new UnsupportedOperationException();
		}

		return m;
	}

	@Override
	public Map<String, T> create( Type t, String u )
	{
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
		switch( node.getKind() ) {
			case NULL:
			break;

			case ITEM: {
				m.put( node.getPath(), node.getValue() );
			}
			break;

			case LINK:
			case NODE: {
				node.<Collection<ConfigNode>> getValue()
					.forEach( n -> fillMap( n, m ) );
			}
		}
	}

}
