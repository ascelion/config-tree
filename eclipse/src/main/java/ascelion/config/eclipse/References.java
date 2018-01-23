
package ascelion.config.eclipse;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import static java.util.Collections.synchronizedMap;

final class References<T>
{

	private final Map<ClassLoader, WeakReference<T>> references = synchronizedMap( new WeakHashMap<ClassLoader, WeakReference<T>>() );

	void put( ClassLoader cld, T obj )
	{
		synchronized( this.references ) {
			this.references.put( cld, new WeakReference<>( obj ) );
		}
	}

	T get( ClassLoader cld, Function<ClassLoader, T> sup )
	{
		T obj = ref( cld );

		if( obj == null ) {
			synchronized( this.references ) {
				obj = ref( cld );

				if( obj == null ) {
					obj = sup.apply( cld );

					this.references.put( cld, new WeakReference<>( obj ) );
				}

				purge( null );
			}
		}

		return obj;
	}

	void remove( T t )
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
