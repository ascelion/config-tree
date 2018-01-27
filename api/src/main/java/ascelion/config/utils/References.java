
package ascelion.config.utils;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;

public final class References<T>
{

	static private final Logger L = Logger.getLogger( References.class.getName() );

	private final Map<ClassLoader, WeakReference<T>> references = synchronizedMap( new WeakHashMap<ClassLoader, WeakReference<T>>() );

	public void put( ClassLoader cld, T obj )
	{
		synchronized( this.references ) {
			this.references.put( cld, new WeakReference<>( obj ) );
		}
	}

	public T get( ClassLoader cld, Function<ClassLoader, T> sup )
	{
		Objects.requireNonNull( cld, "The classLoader cannot be null" );

		T obj = ref( cld );

		if( obj == null ) {
			synchronized( this.references ) {
				obj = ref( cld );

				if( obj == null ) {
					obj = sup.apply( cld );

					final WeakReference<T> ref = new WeakReference<>( obj );

					L.finest( () -> format( "PUT %s / %s / %s", cld, ref, ref.get() ) );

					this.references.put( cld, ref );
				}

				purge( null );
			}
		}

		return obj;
	}

	public void remove( T t )
	{
		synchronized( this.references ) {
			purge( t );
		}
	}

	private void purge( T t )
	{
		final Iterator<Map.Entry<ClassLoader, WeakReference<T>>> it = this.references.entrySet().iterator();

		while( it.hasNext() ) {
			final Map.Entry<ClassLoader, WeakReference<T>> ent = it.next();
			final T obj = ent.getValue().get();

			if( obj == null || obj == t ) {
				L.finest( () -> format( "DEL %s / %s / %s", ent.getKey(), ent.getValue(), obj ) );

				it.remove();
			}
		}
	}

	private T ref( ClassLoader cld )
	{
		final WeakReference<T> ref = this.references.get( cld );

		return ref != null ? ref.get() : null;
	}
}
