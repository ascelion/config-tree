
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

import static ascelion.config.impl.Utils.values;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

class SetConverter<T> implements ConfigConverter<Set<T>>
{

	private final Type type;
	private final ConfigConverter<T> conv;

	SetConverter( Type type, ConfigConverter<T> conv )
	{
		this.type = type;
		this.conv = conv;
	}

	@Override
	public Set<T> create( Type t, String u )
	{
		final String[] v = values( u );

		return unmodifiableSet( Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.collect( toSet() ) );
	}
}
