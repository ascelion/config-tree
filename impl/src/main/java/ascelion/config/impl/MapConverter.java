
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

final class MapConverter<T> implements ConfigConverter<Map<String, T>>
{

	private final ConfigConverter<T> conv;
	private final ConfigNode root;
	private final int unwrap;

	MapConverter( ConfigConverter<T> conv, ConfigNode root, int unwrap )
	{
		this.conv = conv;
		this.root = root;
		this.unwrap = unwrap;
	}

	@Override
	public Map<String, T> create( Type t, String u )
	{
		final String eval = this.root.getValue( u );
		final ConfigNode node = this.root.getNode( eval != null ? eval : u );

		if( node == null ) {
			return emptyMap();
		}

		return node.asMap( this.unwrap )
			.entrySet()
			.stream()
			.collect( toMap( e -> e.getKey(), e -> this.conv.create( t, e.getValue() ) ) );
	}

	@Override
	public Map<String, T> create( Class<? super Map<String, T>> t, String u )
	{
		final ParameterizedType p = (ParameterizedType) t.getGenericSuperclass();

		return create( p.getActualTypeArguments()[1], u );
	}
}
