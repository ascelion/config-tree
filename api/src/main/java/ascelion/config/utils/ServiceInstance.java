
package ascelion.config.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Function;

import static java.lang.String.format;

public final class ServiceInstance<T>
{

	@Target( ElementType.FIELD )
	@Retention( RetentionPolicy.RUNTIME )
	public @interface CLD
	{
	}

	private final References<T> services = new References<>();
	private final Class<T> type;
	private final String name;

	public ServiceInstance( Class<T> type )
	{
		this.type = type;
		this.name = type.getName();
	}

	public T get()
	{
		return get( null );
	}

	public T get( ClassLoader cld )
	{
		return this.services.get( cld, this::create );
	}

	public void set( T instance )
	{
		set( null, instance );
	}

	public void set( ClassLoader cld, T instance )
	{
		this.services.put( cld, instance );
	}

	public void clear()
	{
		this.services.clear();
	}

	public T create( ClassLoader cld )
	{
		T instance = loadFromProperty( cld );
		if( instance != null ) {
			return withClassLoader( cld, instance );
		}

		instance = loadFromService( cld );
		if( instance != null ) {
			return withClassLoader( cld, instance );
		}

		instance = fromFactory( cld, "Factory" );
		if( instance != null ) {
			return withClassLoader( cld, instance );
		}

		instance = fromFactory( cld, "$Factory" );
		if( instance != null ) {
			return withClassLoader( cld, instance );
		}

		throw new ServiceConfigurationError( "Cannot find any implementation of " + this.name );
	}

	private T fromFactory( ClassLoader cld, String suffix )
	{
		final String factName = this.name + suffix;
		final Class<Function<ClassLoader, T>> factClass;

		try {
			factClass = (Class<Function<ClassLoader, T>>) cld.loadClass( factName );
		}
		catch( final ClassNotFoundException e ) {
			return null;
		}

		try {
			final Constructor<Function<ClassLoader, T>> ct = factClass.getDeclaredConstructor();

			ct.setAccessible( true );

			return ct.newInstance().apply( cld );
		}
		catch( InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e ) {
			throw new IllegalStateException( format( "Cannot instantiate %s from %s ", this.name, factName ), e );
		}
	}

	private T withClassLoader( ClassLoader cld, T instance )
	{
		for( Class<?> c = instance.getClass(); c != Object.class; c = c.getSuperclass() ) {
			for( final Field f : c.getDeclaredFields() ) {
				final int m = f.getModifiers();
				if( Modifier.isStatic( m ) || Modifier.isFinal( m ) ) {
					continue;
				}
				if( f.getType() == ClassLoader.class && f.isAnnotationPresent( CLD.class ) ) {
					f.setAccessible( true );
					try {
						f.set( instance, cld );
					}
					catch( IllegalArgumentException | IllegalAccessException e ) {
						throw new ServiceConfigurationError( format( "Cannot inject associated classloader into %s.%s", f.getDeclaringClass().getName(), f.getName() ) );
					}
				}
			}
		}

		return instance;
	}

	private T loadFromService( ClassLoader cld )
	{
		if( cld == null ) {
			return null;
		}

		T instance = null;

		for( final T reg : ServiceLoader.load( this.type, cld ) ) {
			if( instance != null ) {
				throw new ServiceConfigurationError( format( "Found multiple implementations of %1$s, use -D%1$s=some.package.Implementation", this.name ) );
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
