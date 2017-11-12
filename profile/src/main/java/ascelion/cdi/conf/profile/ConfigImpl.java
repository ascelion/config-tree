
package ascelion.cdi.conf.profile;

import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class ConfigImpl implements Config
{

	@Override
	public <T> T getValue( String propertyName, Class<T> propertyType )
	{
		return null;
	}

	@Override
	public <T> Optional<T> getOptionalValue( String propertyName, Class<T> propertyType )
	{
		return null;
	}

	@Override
	public Iterable<String> getPropertyNames()
	{
		return null;
	}

	@Override
	public Iterable<ConfigSource> getConfigSources()
	{
		return null;
	}

}
