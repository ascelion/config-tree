
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

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

	private final Type type;
	private final ConfigConverter<T> conv;

	SetConverter( ConfigConverter<T> conv )
	{
		final Type sc = getClass().getGenericSuperclass();

		if( !( sc instanceof ParameterizedType ) ) {
			throw new IllegalArgumentException( "No type info" );
		}

		final ParameterizedType pt = (ParameterizedType) sc;

		this.type = pt.getActualTypeArguments()[0];
		this.conv = conv;
	}

	@Override
	public Set<T> create( Class<? super Set<T>> t, String u )
	{
		return create( (Type) t, u );
	}

	@Override
	public Set<T> create( Type t, String u )
	{
		final String[] v = Utils.toArray( u );

		return Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.collect( toSet() );
	}
}
