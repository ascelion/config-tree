
package ascelion.config.conv;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static ascelion.config.conv.Utils.values;

class ArrayConverter<T> extends WrapConverter<T[], T>
{

	ArrayConverter( Type type, ConfigConverter<T> conv )
	{
		super( type, conv );
	}

	@Override
	public T[] create( String u )
	{
		final String[] v = values( u );

		return Stream.of( v )
			.map( x -> this.conv.create( x ) )
			.toArray( this::newArray );
	}

	@Override
	public T[] create( ConfigNode u, int unwrap )
	{
		return create( u != null ? u.<String> getValue() : null );
	}

	private T[] newArray( int n )
	{
		return (T[]) Array.newInstance( (Class<?>) this.type, n );
	}
}
