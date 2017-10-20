
package ascelion.shared.cdi.conf;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
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
	@interface Reload
	{

		int value() default -1;

		TimeUnit unit() default TimeUnit.SECONDS;
	}

	@Retention( RUNTIME )
	@Target( TYPE )
	@Qualifier
	@ApplicationScoped
	@interface Type
	{

		String value();

		@Nonbinding
		String[] types() default {};

		@Nonbinding
		Reload reload() default @Reload( );
	}

	@Retention( RUNTIME )
	@Target( TYPE )
	@interface List
	{

		ConfigSource[] value();
	}

	String value() default "";

	int priority() default 0;

	String type() default "";

	Reload reload() default @Reload( );
}
