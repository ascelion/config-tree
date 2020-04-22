
package ascelion.config.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention( RUNTIME )
@Target( TYPE )
@Repeatable( ConfigSource.Array.class )
public @interface ConfigSource
{

	@Retention( RUNTIME )
	@Target( TYPE )
	@interface Array
	{

		ConfigSource[] value();
	}

	String value() default "";
}
