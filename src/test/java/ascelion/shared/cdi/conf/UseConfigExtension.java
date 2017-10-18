
package ascelion.shared.cdi.conf;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;

import ascelion.tests.cdi.AdditionalAnnotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention( RUNTIME )
@Target( TYPE )
@AdditionalAnnotations(
	annotations = {
		ApplicationScoped.class,
	},
	fromPackages = {
		ConfigExtension.class,
	} )
public @interface UseConfigExtension
{
}
