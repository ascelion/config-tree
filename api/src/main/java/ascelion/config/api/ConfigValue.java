
package ascelion.config.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@Retention( RUNTIME )
@Target( { METHOD, FIELD, PARAMETER, TYPE } )
public @interface ConfigValue
{

	@Builder( builderClassName = "Builder", builderMethodName = "from" )
	@AllArgsConstructor( access = AccessLevel.PRIVATE )
	@ToString
	class Literal implements ConfigValue
	{

		public static Builder from( ConfigValue cval, String value )
		{
			return new Builder()
				.value( value )
				.required( cval.required() )
				.usePrefix( cval.usePrefix() )
				.keyUnwrap( cval.keyUnwrap() );
		}

		private final String value;
		private final boolean required;
		private final boolean usePrefix;
		private final boolean keyUnwrap;

		@Override
		public Class<? extends Annotation> annotationType()
		{
			return ConfigValue.class;
		}

		@Override
		public String value()
		{
			return this.value;
		}

		@Override
		public boolean required()
		{
			return this.required;
		}

		@Override
		public boolean usePrefix()
		{
			return this.usePrefix;
		}

		@Override
		public boolean keyUnwrap()
		{
			return this.keyUnwrap;
		}
	}

	String value() default "";

	boolean usePrefix() default true;

	boolean keyUnwrap() default false;

	boolean required() default true;
}
