
package ascelion.cdi.conf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
class DefaultCVT implements BiFunction<Class<Object>, String, Object>
{

	@Override
	public Object apply( Class<Object> t, String u )
	{
		if( (Object) Class.class == t ) {
			try {
				return Thread.currentThread().getContextClassLoader().loadClass( u );
			}
			catch( final ClassNotFoundException e ) {
				throw new RuntimeException( e );
			}
		}

		try {
			final Method m = t.getMethod( "valueOf", String.class );

			try {
				return m.invoke( null, u );
			}
			catch( IllegalAccessException | InvocationTargetException e ) {
				throw new RuntimeException( e );
			}
		}
		catch( final NoSuchMethodException e ) {
		}

		try {
			final Constructor<?> c = t.getConstructor( String.class );

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
