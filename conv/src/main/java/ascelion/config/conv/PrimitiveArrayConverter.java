
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static ascelion.config.conv.Utils.values;

class PrimitiveArrayConverter<X> extends WrapConverter<Object, X>
{

	private final Class<?> type;

	PrimitiveArrayConverter( Type type, ConfigConverter<X> conv )
	{
		super( type, conv );

		if( !( type instanceof Class ) && !( (Class) type ).isPrimitive() ) {
			throw new IllegalArgumentException( "Expecting primitive type" );
		}

		this.type = (Class<?>) type;
	}

	@Override
	public Object create( ConfigNode u, int unwrap )
	{
		return create( u != null ? u.<String> getValue() : null );
	}

	@Override
	public Object create( String u )
	{
		final String[] v = values( u );
		final Stream<?> s = Stream.of( v ).map( x -> this.conv.create( x ) );

		if( this.type == byte.class || this.type == short.class || this.type == int.class ) {
			return s.map( Number.class::cast ).mapToInt( Number::intValue ).toArray();
		}
		if( this.type == long.class ) {
			return s.map( Number.class::cast ).mapToLong( Number::longValue ).toArray();
		}
		if( this.type == float.class || this.type == double.class ) {
			return s.map( Number.class::cast ).mapToDouble( Number::doubleValue ).toArray();
		}

		throw new IllegalStateException( "Cannot handle " + this.type );
	}
}
