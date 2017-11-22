
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

import static ascelion.config.impl.Utils.values;
import static java.util.stream.Collectors.toSet;

class SetConverter<T> implements ConfigConverter<Set<T>>
{

	static class IntSet extends SetConverter<Integer>
	{

		IntSet( ConfigConverter<Integer> conv )
		{
			super( conv );
		}
	}

	static class LongSet extends SetConverter<Long>
	{

		LongSet( ConfigConverter<Long> conv )
		{
			super( conv );
		}
	}

	static class DoubleSet extends SetConverter<Double>
	{

		DoubleSet( ConfigConverter<Double> conv )
		{
			super( conv );
		}
	}

	static class StringSet extends SetConverter<String>
	{

		StringSet( ConfigConverter<String> conv )
		{
			super( conv );
		}
	}

	private final Type type;
	private final ConfigConverter<T> conv;

	SetConverter( ConfigConverter<T> conv )
	{
		final Type ct = Utils.converterType( getClass() );

		if( !( ct instanceof ParameterizedType ) ) {
			throw new IllegalArgumentException( "No type info" );
		}

		final ParameterizedType pt = (ParameterizedType) ct;

		this.type = pt.getActualTypeArguments()[0];
		this.conv = conv;
	}

	@Override
	public Set<T> create( Type t, String u, int unwrap )
	{
		final String[] v = values( u );

		return Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.collect( toSet() );
	}
}
