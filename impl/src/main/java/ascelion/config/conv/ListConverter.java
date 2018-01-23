
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.impl.Utils;

import static ascelion.config.impl.Utils.values;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

class ListConverter<T> implements ConfigConverter<List<T>>
{

	private final Type type;
	private final ConfigConverter<T> conv;

	ListConverter( Type type, ConfigConverter<T> conv )
	{
		this.type = type;
		this.conv = conv;
	}

	@Override
	public List<T> create( Type t, ConfigNode u, int unwrap )
	{
		if( Utils.isContainer( this.type ) ) {
			final List<T> c = new ArrayList<>();
			final Collection<ConfigNode> nodes = u.getNodes();

			if( nodes != null ) {
				nodes.forEach( n -> {
					c.add( this.conv.create( this.type, n, unwrap ) );
				} );
			}

			return c;
		}
		else {
			return ConfigConverter.super.create( t, u, unwrap );
		}
	}

	@Override
	public List<T> create( Type t, String u )
	{
		final String[] v = values( u );

		return unmodifiableList( Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.collect( toList() ) );
	}

}
