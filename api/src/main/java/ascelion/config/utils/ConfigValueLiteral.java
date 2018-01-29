
package ascelion.config.utils;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigValue;

public final class ConfigValueLiteral extends AnnotationLiteral<ConfigValue> implements ConfigValue
{

	private final String value;
	private final Class<? extends ConfigConverter> converter;
	private final int unwrap;

	public ConfigValueLiteral( String value, Class<? extends ConfigConverter> converter, int unwrap )
	{
		this.value = value;
		this.converter = converter;
		this.unwrap = unwrap;
	}

	public ConfigValueLiteral( String value )
	{
		this( value, ConfigConverter.class, 0 );
	}

	@Override
	public String value()
	{
		return this.value;
	}

	@Override
	public Class<? extends ConfigConverter> converter()
	{
		return this.converter;
	}

	@Override
	public int unwrap()
	{
		return this.unwrap;
	}
}
