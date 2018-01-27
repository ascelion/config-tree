
package ascelion.config.utils;

import java.security.PrivilegedAction;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;

public final class ServiceInstance<T>
{

	static public ClassLoader classLoader( ClassLoader cld, Class<?> fallback )
	{
		if( cld != null ) {
			return cld;
		}

		cld = doPrivileged( (PrivilegedAction<ClassLoader>) () -> currentThread().getContextClassLoader() );
		if( cld != null ) {
			return cld;
		}

		if( fallback != null ) {
			cld = fallback.getClassLoader();
			if( cld != null ) {
				cld = ClassLoader.getSystemClassLoader();
			}
		}

		return doPrivileged( (PrivilegedAction<ClassLoader>) () -> ClassLoader.getSystemClassLoader() );
	}

	private final Class<T> type;
	private final String name;
	private volatile T instance;

	public ServiceInstance( Class<T> type )
	{
		this.type = type;
		this.name = type.getName();
	}

	public T get()
	{
		if( this.instance != null ) {
			return this.instance;
		}

		synchronized( this ) {
			if( this.instance != null ) {
				return this.instance;
			}

			final ClassLoader cld = classLoader( null, this.type );

			this.instance = loadFromProperty( cld );

			if( this.instance == null ) {
				this.instance = loadFromService( cld );
			}

			if( this.instance == null ) {
				throw new IllegalStateException( "No ConfigRegistry implementations found" );
			}

			return this.instance;
		}
	}

	public void set( T instance )
	{
		this.instance = instance;
	}

	private T loadFromService( ClassLoader cld )
	{
		if( cld == null ) {
			return null;
		}

		T instance = null;

		for( final T reg : ServiceLoader.load( this.type, cld ) ) {
			if( instance != null ) {
				throw new IllegalStateException( format( "Multiple ConfigRegistry implementations found, use -D%s=some.package.Implementation", this.name ) );
			}

			instance = reg;
		}

		return instance;
	}

	private T loadFromProperty( ClassLoader cld )
	{
		final String className = System.getProperty( this.name );

		if( className == null || className.isEmpty() ) {
			return null;
		}

		try {
			return (T) cld.loadClass( className ).newInstance();
		}
		catch( InstantiationException | IllegalAccessException | ClassNotFoundException e ) {
			Logger.getLogger( this.name )
				.log( Level.CONFIG, e, () -> "Cannot load class " + className );

			return null;
		}
	}
}
