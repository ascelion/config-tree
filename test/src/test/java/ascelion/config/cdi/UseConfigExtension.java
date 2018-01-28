
package ascelion.config.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ascelion.cdi.junit.EnableExtensions;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention( RUNTIME )
@Target( TYPE )
@EnableExtensions( {
	ConfigExtension.class
} )
public @interface UseConfigExtension
{
}
