
package ascelion.config.utils;

import java.security.PrivilegedAction;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.security.AccessController.doPrivileged;

public final class ServiceInstance<T>
{

	private final Class<T> type;
	private final String name;
	private final Supplier<T> impl;
	private volatile T instance;

	public ServiceInstance( Class<T> type )
	{
		this( type, () -> null );
	}

	public ServiceInstance( Class<T> type, Supplier<T> impl )
	{
		this.type = type;
		this.name = type.getName();
		this.impl = impl;
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

			return this.instance = doPrivileged( (PrivilegedAction<T>) () -> load() );
		}
	}

	public void set( T instance )
	{
		this.instance = instance;
	}

	private T load()
	{
		final ClassLoader cld = Utils.classLoader( null, this.type );

		T instance = loadFromProperty( cld );
		if( instance != null ) {
			return instance;
		}

		instance = loadFromService( cld );
		if( instance != null ) {
			return instance;
		}

		instance = this.impl.get();
		if( instance != null ) {
			return instance;
		}

		throw new ServiceConfigurationError( "No ConfigRegistry implementations found" );
	}

	private T loadFromService( ClassLoader cld )
	{
		if( cld == null ) {
			return null;
		}

		T instance = null;

		for( final T reg : ServiceLoader.load( this.type, cld ) ) {
			if( instance != null ) {
				throw new ServiceConfigurationError( format( "Multiple ConfigRegistry implementations found, use -D%s=some.package.Implementation", this.name ) );
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
			throw new IllegalStateException( "Cannot instantiate type " + className, e );
		}
	}
}
