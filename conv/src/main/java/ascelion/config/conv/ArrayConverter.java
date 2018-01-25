
package ascelion.config.conv;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static ascelion.config.conv.Utils.values;

class ArrayConverter<T> extends WrapConverter<Object, T>
{

	ArrayConverter( Type type, ConfigConverter<T> conv )
	{
		super( type, conv );
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

		if( ( this.type instanceof Class ) && ( (Class) this.type ).isPrimitive() ) {
			final Object a = newArray( v.length );

			for( int k = 0; k < v.length; k++ ) {
				Array.set( a, k, this.conv.create( v[k] ) );
			}

			return a;
		}
		else {
			return Stream.of( v )
				.map( x -> this.conv.create( x ) )
				.toArray( n -> (Object[]) newArray( n ) );
		}
	}

	private Object newArray( int n )
	{
		return this.type instanceof Class ? Array.newInstance( (Class<?>) this.type, n ) : Array.newInstance( Object.class, n );
	}
}
