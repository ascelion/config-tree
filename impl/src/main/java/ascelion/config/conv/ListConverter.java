
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

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
	public List<T> create( Type t, String u )
	{
		final String[] v = values( u );

		return unmodifiableList( Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.collect( toList() ) );
	}

}
