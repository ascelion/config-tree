
package ascelion.shared.cdi.conf;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.BiFunction;

import javax.enterprise.util.AnnotationLiteral;
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

	class Literal extends AnnotationLiteral<ConfigValue> implements ConfigValue
	{

		private final String value;
		private final int unwrap;
		private final Class<? extends BiFunction> converter;

		public Literal( String value, int unwrap, Class<? extends BiFunction> converter )
		{
			this.value = value;
			this.unwrap = unwrap;
			this.converter = converter;
		}

		public Literal( ConfigValue annotation, Class<? extends BiFunction> converter )
		{
			this( annotation.value(), annotation.unwrap(), converter );
		}

		@Override
		public String value()
		{
			return this.value;
		}

		@Override
		public int unwrap()
		{
			return this.unwrap;
		}

		@Override
		public Class<? extends BiFunction> converter()
		{
			return this.converter;
		}
	}

	@Nonbinding
	String value();

	@Nonbinding
	int unwrap() default 0;

	@Nonbinding
	Class<? extends BiFunction> converter() default BiFunction.class;
}
