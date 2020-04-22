
package ascelion.config.cdi;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
final class Primitives
{

	static private Map<Class<?>, Object> DEFAULTS = new HashMap<Class<?>, Object>()
	{

		{
			add( boolean.class, false );
			add( char.class, '0' );
			add( short.class, (short) 0 );
			add( int.class, 0 );
			add( long.class, 0L );
			add( float.class, 0F );
			add( double.class, 0D );
		}

		private <T> void add( Class<T> c, T t )
		{
			put( c, t );
		}
	};

	@SuppressWarnings( "unchecked" )
	static <T> T toDefault( Class<T> c )
	{
		return (T) DEFAULTS.get( c );
	}

	@SuppressWarnings( "unchecked" )
	static <T> T toDefault( Type c )
	{
		return (T) DEFAULTS.get( c );
	}
}
