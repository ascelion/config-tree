
package ascelion.config.impl;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

class ArrayConverter<T> implements ConfigConverter<T[]>
{

	static class IntArray extends ArrayConverter<Integer>
	{

		IntArray( ConfigConverter<Integer> conv )
		{
			super( conv );
		}
	}

	static class LongArray extends ArrayConverter<Long>
	{

		LongArray( ConfigConverter<Long> conv )
		{
			super( conv );
		}
	}

	static class DoubleArray extends ArrayConverter<Double>
	{

		DoubleArray( ConfigConverter<Double> conv )
		{
			super( conv );
		}
	}

	private final Class<T> type;
	private final ConfigConverter<T> conv;

	ArrayConverter( ConfigConverter<T> conv )
	{
		final Type sc = getClass().getGenericSuperclass();

		if( !( sc instanceof ParameterizedType ) ) {
			throw new IllegalArgumentException( "No type info" );
		}

		final ParameterizedType pt = (ParameterizedType) sc;

		this.type = (Class<T>) pt.getActualTypeArguments()[0];
		this.conv = conv;
	}

	@Override
	public T[] create( Type t, String u )
	{
		final String[] v = Utils.toArray( u );

		return Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.toArray( n -> (T[]) Array.newInstance( this.type, n ) );
	}

	@Override
	public T[] create( Class<? super T[]> t, String u )
	{
		return create( (Type) t, u );
	}

}
