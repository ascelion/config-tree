
package ascelion.config.conv;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static ascelion.config.impl.Utils.values;

class ArrayConverter<T> implements ConfigConverter<T[]>
{

	private final Type type;
	private final ConfigConverter<T> conv;

	ArrayConverter( Type type, ConfigConverter<T> conv )
	{
		this.type = type;
		this.conv = conv;
	}

	@Override
	public T[] create( Type t, String u )
	{
		final String[] v = values( u );

		return Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.toArray( this::newArray );
	}

	@Override
	public T[] create( Type t, ConfigNode u, int unwrap )
	{
		return create( t, u != null ? u.<String> getValue() : null );
	}

	private T[] newArray( int n )
	{
		return (T[]) Array.newInstance( (Class<?>) this.type, n );
	}
}
