
package ascelion.config.utils;

import javax.enterprise.util.AnnotationLiteral;

import ascelion.config.api.ConfigSource;

public final class ConfigSourceLiteral extends AnnotationLiteral<ConfigSource> implements ConfigSource
{

	private final String value;
	private final int priority;
	private final String type;

	public ConfigSourceLiteral( String value, int priority, String type )
	{
		this.value = value;
		this.priority = priority;
		this.type = type;
	}

	@Override
	public String value()
	{
		return this.value;
	}

	@Override
	public int priority()
	{
		return this.priority;
	}

	@Override
	public String type()
	{
		return this.type;
	}

}
