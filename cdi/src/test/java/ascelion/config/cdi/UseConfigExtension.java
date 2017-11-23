
package ascelion.config.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.jglue.cdiunit.AdditionalClasspaths;

@Retention( RUNTIME )
@Target( TYPE )
@AdditionalClasspaths( {
	ConfigExtension.class
} )
public @interface UseConfigExtension
{
}
