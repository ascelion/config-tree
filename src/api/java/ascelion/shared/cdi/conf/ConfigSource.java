
package ascelion.shared.cdi.conf;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention( RUNTIME )
@Target( TYPE )
@Repeatable( ConfigSource.List.class )
public @interface ConfigSource
{

	@Retention( RUNTIME )
	@Target( TYPE )
	@Qualifier
	@interface Type
	{

		String value();

		@Nonbinding
		String[] types() default {};
	}

	@Retention( RUNTIME )
	@Target( TYPE )
	@interface List
	{

		ConfigSource[] value();
	}

	String value();

	int priority() default 0;

	Type type() default @Type( "" );
}
