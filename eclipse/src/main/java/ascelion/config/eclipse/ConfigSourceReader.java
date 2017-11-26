
package ascelion.config.eclipse;

import java.util.Map;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.impl.ConfigSourceLiteral;

import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigSourceReader implements ConfigReader
{

	final ConfigSource cs;
	final ascelion.config.api.ConfigSource csa;

	ConfigSourceReader( ConfigSource cs )
	{
		this.cs = cs;
		this.csa = new ConfigSourceLiteral( cs.getName(), cs.getOrdinal(), getClass().getName() );
	}

	@Override
	public Map<String, ?> readConfiguration( ascelion.config.api.ConfigSource source ) throws ConfigException
	{
		return this.cs.getProperties();
	}
}
