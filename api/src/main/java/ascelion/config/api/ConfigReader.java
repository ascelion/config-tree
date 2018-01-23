
package ascelion.config.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface ConfigReader
{

	@Retention( RUNTIME )
	@Target( TYPE )
	@Qualifier
	@Singleton
	@interface Type
	{

		String value();

		@Nonbinding
		String[] types() default {};
	}

	default boolean isModified( ConfigSource source )
	{
		return false;
	}

	Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException;
}
