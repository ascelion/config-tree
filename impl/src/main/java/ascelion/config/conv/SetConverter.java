
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

import static ascelion.config.impl.Utils.values;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

class SetConverter<T> extends WrapConverter<Set<T>, T>
{

	SetConverter( Type type, ConfigConverter<T> conv )
	{
		super( type, conv );
	}

	@Override
	public Set<T> create( String u )
	{
		final String[] v = values( u );

		return unmodifiableSet( Stream.of( v )
			.map( x -> this.conv.create( x ) )
			.collect( toSet() ) );
	}
}
