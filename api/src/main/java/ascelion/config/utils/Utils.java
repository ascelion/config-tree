
package ascelion.config.utils;

import java.security.PrivilegedAction;

import javax.annotation.Priority;

import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;

public final class Utils
{

	static public int getPriority( Object o )
	{
		for( Class<?> cls = o instanceof Class ? (Class<?>) o : o.getClass(); cls != Object.class; cls = cls.getSuperclass() ) {
			final Priority ap = cls.getAnnotation( Priority.class );

			if( ap != null ) {
				return ap.value();
			}
		}

		return 100;
	}

	public static ClassLoader classLoader( ClassLoader cld, Class<?> fallback )
	{
		if( cld != null ) {
			return cld;
		}

		return doPrivileged( (PrivilegedAction<ClassLoader>) () -> {
			ClassLoader cl = currentThread().getContextClassLoader();

			if( cl != null ) {
				return cl;
			}

			if( fallback != null ) {
				cl = fallback.getClassLoader();

				if( cl != null ) {
					return cl;
				}
			}

			return ClassLoader.getSystemClassLoader();
		} );
	}

	private Utils()
	{
	}

}
