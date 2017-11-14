
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;

import static java.util.stream.Collectors.toList;

abstract class ListConverter<T> implements ConfigConverter<List<T>>
{

	static class IntList extends ListConverter<Integer>
	{

		IntList( ConfigConverter<Integer> conv )
		{
			super( conv );
		}
	}

	static class LongList extends ListConverter<Long>
	{

		LongList( ConfigConverter<Long> conv )
		{
			super( conv );
		}
	}

	static class DoubleList extends ListConverter<Double>
	{

		DoubleList( ConfigConverter<Double> conv )
		{
			super( conv );
		}
	}

	static class StringList extends ListConverter<String>
	{

		StringList( ConfigConverter<String> conv )
		{
			super( conv );
		}
	}

	private final Type type;
	private final ConfigConverter<T> conv;

	ListConverter( ConfigConverter<T> conv )
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
	public List<T> create( Type t, String u )
	{
		final String[] v = Utils.toArray( u );

		return Stream.of( v )
			.map( x -> this.conv.create( this.type, x ) )
			.collect( toList() );
	}

	@Override
	public List<T> create( Class<? super List<T>> t, String u )
	{
		return create( (Type) t, u );
	}

}
