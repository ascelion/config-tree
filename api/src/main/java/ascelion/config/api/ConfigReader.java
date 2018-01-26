
package ascelion.config.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.util.Nonbinding;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface ConfigReader
{

	@Retention( RUNTIME )
	@Target( TYPE )
	@interface Type
	{

		String value();

		@Nonbinding
		String[] types() default {};
	}

	default Set<String> types()
	{
		final Type a = getClass().getAnnotation( Type.class );

		if( a != null ) {
			final Set<String> set = new TreeSet<>();

			set.add( a.value() );

			for( final String t : a.types() ) {
				set.add( t );
			}

			return set;
		}
		else {
			throw new IllegalStateException( "Must override this method" );
		}
	}

	default boolean isModified( String source )
	{
		return false;
	}

	Map<String, String> readConfiguration( String source ) throws ConfigException;
}
