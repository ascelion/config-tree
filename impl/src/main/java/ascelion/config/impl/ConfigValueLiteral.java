
package ascelion.config.impl;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigValue;

final class ConfigValueLiteral extends AnnotationLiteral<ConfigValue> implements ConfigValue
{

	private final String value;
	private final Class<? extends ConfigConverter> converter;
	private final int unwrap;

	ConfigValueLiteral( String value, Class<? extends ConfigConverter> converter, int unwrap )
	{
		this.value = value;
		this.converter = converter;
		this.unwrap = unwrap;
	}

	ConfigValueLiteral( String[] values, Class<? extends ConfigConverter> converter, int unwrap )
	{
		this( Stream.of( values ).collect( Collectors.joining( Eval.Token.S_DEF ) ), converter, unwrap );
	}

	ConfigValueLiteral( String value )
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
