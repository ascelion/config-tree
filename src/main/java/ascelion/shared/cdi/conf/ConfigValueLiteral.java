
package ascelion.shared.cdi.conf;

import java.util.function.BiFunction;

import javax.enterprise.util.AnnotationLiteral;

final class ConfigValueLiteral extends AnnotationLiteral<ConfigValue> implements ConfigValue
{

	private final String value;
	private final Class<? extends BiFunction> converter;
	private final int unwrap;

	ConfigValueLiteral( String value, Class<? extends BiFunction> converter, int unwrap )
	{
		this.value = value;
		this.converter = converter;
		this.unwrap = unwrap;
	}

	@Override
	public String value()
	{
		return this.value;
	}

	@Override
	public Class<? extends BiFunction> converter()
	{
		return this.converter;
	}

	@Override
	public int unwrap()
	{
		return this.unwrap;
	}
}
