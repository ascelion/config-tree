
package ascelion.config.utils;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import static java.lang.String.format;

public final class ServiceInstance<T>
{

	private final References<T> refs = new References<>();
	private final Class<T> type;
	private final String name;
	private final Supplier<T> impl;

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
		return get( null );
	}

	public T get( ClassLoader cld )
	{
		return this.refs.get( cld, this::load );
	}

	public void set( T instance )
	{
		set( null, instance );
	}

	public void set( ClassLoader cld, T instance )
	{
		this.refs.put( cld, instance );
	}

	private T load( ClassLoader cld )
	{
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
