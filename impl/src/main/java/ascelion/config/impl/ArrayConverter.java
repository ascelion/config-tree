
package ascelion.config.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

import static ascelion.config.impl.Utils.values;
import static java.lang.String.format;

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

	static class StringArray extends ArrayConverter<String>
	{

		StringArray( ConfigConverter<String> conv )
		{
			super( conv );
		}
	}

	private final Class<T> type;
	private final ConfigConverter<T> conv;

	ArrayConverter( ConfigConverter<T> conv )
	{
		final Type ct = Utils.converterType( getClass() );

		if( ct instanceof Class ) {
			this.type = (Class<T>) ( (Class<?>) ct ).getComponentType();
		}
		else {
			throw new IllegalArgumentException( format( "Cannot convert from String to %s", ct ) );
		}

		this.conv = conv;
	}

	@Override
	public T[] create( Type t, String u, int unwrap )
	{
		final String[] v = values( u );

		return Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.toArray( n -> (T[]) Array.newInstance( this.type, n ) );
	}

}
