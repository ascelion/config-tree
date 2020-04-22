
package ascelion.config.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Retention(RUNTIME)
@Target(TYPE)
public @interface ConfigPrefix {
	@RequiredArgsConstructor
	@ToString
	class Literal implements ConfigPrefix {
		private final String value;

		@Override
		public Class<? extends Annotation> annotationType() {
			return ConfigPrefix.class;
		}

		@Override
		public String value() {
			return this.value;
		}

	}

	String value();
}
