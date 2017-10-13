
package ascelion.shared.cdi.conf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention( RUNTIME )
@Target( { PARAMETER, FIELD, METHOD, CONSTRUCTOR, ANNOTATION_TYPE } )
@Qualifier
@Documented
public @interface ConfigValue
{

	@Nonbinding
	String value();

	@Nonbinding
	String unwrap() default "";

	@Nonbinding
	Class<? extends BiFunction> converter() default BiFunction.class;
}
