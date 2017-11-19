
package ascelion.config.api;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention( RUNTIME )
@Target( TYPE )
@Repeatable( ConfigSource.List.class )
public @interface ConfigSource
{

	@Retention( RUNTIME )
	@Target( TYPE )
	@interface List
	{

		ConfigSource[] value();
	}

	String value() default "";

	int priority() default 100;

	String type() default "";
}
