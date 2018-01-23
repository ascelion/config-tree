
package ascelion.config.eclipse;

import java.util.Map;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.impl.ConfigSourceLiteral;

import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigSourceReader implements ConfigReader
{

	@Getter( AccessLevel.PACKAGE )
	private final ConfigSource source;
	private final ascelion.config.api.ConfigSource annotation;

	ConfigSourceReader( ConfigSource cs )
	{
		this.source = cs;
		this.annotation = new ConfigSourceLiteral( cs.getName(), cs.getOrdinal(), getClass().getName() );
	}

	@Override
	public Map<String, ?> readConfiguration( ascelion.config.api.ConfigSource source ) throws ConfigException
	{
		return this.source.getProperties();
	}

}
