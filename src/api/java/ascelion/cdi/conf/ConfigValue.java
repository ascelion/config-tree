
package ascelion.cdi.conf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention( RUNTIME )
@Target( { METHOD, FIELD, PARAMETER, TYPE } )
@Qualifier
@Documented
public @interface ConfigValue
{

	@Nonbinding
	String value() default "";

	@Nonbinding
	int unwrap() default 0;

	@Nonbinding
	Class<? extends BiFunction> converter() default BiFunction.class;
}
