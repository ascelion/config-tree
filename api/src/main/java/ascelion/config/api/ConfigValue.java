package ascelion.config.api;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Retention(RUNTIME)
@Target({ METHOD, FIELD, PARAMETER, TYPE })
public @interface ConfigValue {
	@Builder(builderClassName = "Builder", builderMethodName = "from")
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@ToString
	class Literal implements ConfigValue {

		private final String value;
		private final boolean usePrefix;
		private final boolean required;

		public static Builder from(ConfigValue annotation) {
			return new Builder()
					.required(annotation.required())
					.usePrefix(annotation.usePrefix());
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return ConfigValue.class;
		}

		@Override
		public String value() {
			return this.value;
		}

		@Override
		public boolean usePrefix() {
			return this.usePrefix;
		}

		@Override
		public boolean required() {
			return this.required;
		}
	}

	String value() default "";

	boolean usePrefix() default true;

	boolean required() default true;
}
