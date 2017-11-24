
package ascelion.config.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
@Inherited
@Documented
public @interface ConfigValue
{

	@Retention( RUNTIME )
	@Target( { METHOD, FIELD, PARAMETER, TYPE } )
	@interface Default
	{
	}

	@Nonbinding
	String value() default "";

	@Nonbinding
	int unwrap() default 0;

	@Nonbinding
	Class<? extends ConfigConverter> converter() default ConfigConverter.class;
}
