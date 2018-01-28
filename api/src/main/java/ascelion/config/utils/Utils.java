
package ascelion.config.utils;

import java.security.PrivilegedAction;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Priority;

import ascelion.config.api.ConfigNode;

import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;

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

	static public String path( ConfigNode node )
	{
		return node != null ? node.getPath() : null;
	}

	static public String path( int s, int e, String[] names )
	{
		return asList( names ).subList( s, e ).stream().collect( Collectors.joining( "." ) );
	}

	static public String path( String... names )
	{
		return Stream.of( names ).filter( StringUtils::isNotBlank ).collect( Collectors.joining( "." ) );
	}

	static public String[] pathNames( String path )
	{
		return isNotBlank( path ) ? path.split( "\\." ) : new String[0];
	}

	private Utils()
	{
	}

	static public <X> X[] asArray( X... x )
	{
		return x;
	}

	static public <X> Set<? super X> asSet( X... x )
	{
		return asList( x ).stream().collect( Collectors.toSet() );
	}

}
