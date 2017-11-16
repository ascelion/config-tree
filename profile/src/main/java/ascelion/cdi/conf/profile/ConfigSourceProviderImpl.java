
package ascelion.cdi.conf.profile;

import ascelion.config.impl.ConfigJava;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

@ascelion.config.api.ConfigSource( value = "META-INF/microprofile-config.properties" )
public class ConfigSourceProviderImpl implements ConfigSourceProvider
{

	private final ConfigJava java = new ConfigJava();

	@Override
	public Iterable<ConfigSource> getConfigSources( ClassLoader forClassLoader )
	{
		for( final ascelion.config.api.ConfigSource cs : this.java.getSources() ) {
		}

		return null;
	}

}
