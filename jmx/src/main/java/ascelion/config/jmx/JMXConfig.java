
package ascelion.config.jmx;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention( RUNTIME )
@Target( { TYPE, CONSTRUCTOR, METHOD, FIELD } )
@Repeatable( JMXConfig.List.class )
public @interface JMXConfig
{

	@Retention( RUNTIME )
	@Target( { TYPE, METHOD, FIELD } )
	@interface List
	{

		JMXConfig[] value();
	}

	String[] value() default {};

	boolean writable() default false;

	boolean sensitive() default false;
}
