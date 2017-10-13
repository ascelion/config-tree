
package ascelion.shared.cdi.conf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

class DefaultCVT<T> implements BiFunction<Class<T>, String, T>
{

	@Override
	public T apply( Class<T> t, String u )
	{
		try {
			final Method m = t.getMethod( "valueOf", String.class );

			try {
				return (T) m.invoke( null, u );
			}
			catch( IllegalAccessException | InvocationTargetException e ) {
				throw new RuntimeException( e );
			}
		}
		catch( final NoSuchMethodException e ) {
		}

		try {
			final Constructor<T> c = t.getConstructor( String.class );

			c.setAccessible( true );

			try {
				return c.newInstance( u );
			}
			catch( InstantiationException | IllegalAccessException | InvocationTargetException e ) {
				throw new RuntimeException( e );
			}
		}
		catch( final NoSuchMethodException e ) {
			throw new RuntimeException( e );
		}
	}
}
