package ascelion.config.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ METHOD, FIELD, PARAMETER, TYPE })
public @interface ConfigValue {

	String value() default "";

	boolean usePrefix() default true;

	boolean required() default true;

	Class<?> type() default Object.class;
}
