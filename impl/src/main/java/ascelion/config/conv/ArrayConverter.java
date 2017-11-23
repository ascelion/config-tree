
package ascelion.config.conv;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.impl.Utils;

import static ascelion.config.impl.Utils.values;
import static java.lang.String.format;

class ArrayConverter<T> implements ConfigConverter<T[]>
{

//	static class IntArray extends ArrayConverter<Integer>
//	{
//
//		IntArray( ConfigConverter<Integer> conv )
//		{
//			super( conv );
//		}
//	}
//
//	static class LongArray extends ArrayConverter<Long>
//	{
//
//		LongArray( ConfigConverter<Long> conv )
//		{
//			super( conv );
//		}
//	}
//
//	static class DoubleArray extends ArrayConverter<Double>
//	{
//
//		DoubleArray( ConfigConverter<Double> conv )
//		{
//			super( conv );
//		}
//	}
//
//	static class StringArray extends ArrayConverter<String>
//	{
//
//		StringArray( ConfigConverter<String> conv )
//		{
//			super( conv );
//		}
//	}

	private final Class<T> type;
	private final ConfigConverter<T> conv;

	ArrayConverter( ConfigConverter<T> conv )
	{
		final Type ct = Utils.paramType( getClass(), ConfigConverter.class, 0 );

		if( ct instanceof Class ) {
			this.type = (Class<T>) ( (Class<?>) ct ).getComponentType();
		}
		else {
			throw new IllegalArgumentException( format( "Cannot convert from String to %s", ct ) );
		}

		this.conv = conv;
	}

	@Override
	public T[] create( Type t, String u )
	{
		final String[] v = values( u );

		return Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.toArray( n -> (T[]) Array.newInstance( this.type, n ) );
	}

	@Override
	public T[] create( Type t, ConfigNode u, int unwrap )
	{
		return create( t, u != null ? u.<String> getValue() : null );
	}
}
