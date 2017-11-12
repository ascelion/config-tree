
package ascelion.cdi.conf.profile;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public class ConfigSourceProviderImpl implements ConfigSourceProvider
{

	@Override
	public Iterable<ConfigSource> getConfigSources( ClassLoader forClassLoader )
	{
		return null;
	}

}
