
package ascelion.cdi.conf;

import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.shared.cdi.conf.ConfigValue;

final class ConfigValueLiteral extends AnnotationLiteral<ConfigValue> implements ConfigValue
{

	private final String value;
	private final Class<? extends BiFunction> converter;
	private final int unwrap;

	ConfigValueLiteral( String[] n, Class<? extends BiFunction> converter, int unwrap )
	{
		this.value = Stream.of( n ).collect( Collectors.joining( ":" ) );
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
