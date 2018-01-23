
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

class ListConverter<T> extends WrapConverter<List<T>, T>
{

	ListConverter( Type type, ConfigConverter<T> conv )
	{
		super( type, conv );
	}

	@Override
	public List<T> create( ConfigNode u, int unwrap )
	{
		if( Utils.isContainer( this.type ) ) {
			final List<T> c = new ArrayList<>();
			final Collection<ConfigNode> nodes = u.getNodes();

			if( nodes != null ) {
				nodes.forEach( n -> {
					c.add( this.conv.create( n, unwrap ) );
				} );
			}

			return c;
		}
		else {
			return super.create( u, unwrap );
		}
	}

	@Override
	public List<T> create( String u )
	{
		final String[] v = values( u );

		return unmodifiableList( Stream.of( v )
			.map( x -> this.conv.create( x ) )
			.collect( toList() ) );
	}

}
