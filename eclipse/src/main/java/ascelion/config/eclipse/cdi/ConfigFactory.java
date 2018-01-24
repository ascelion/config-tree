
package ascelion.config.eclipse.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

class ConfigFactory
{

	@Produces
	@ApplicationScoped
	static Config getConfig()
	{
		return ConfigProviderResolver.instance().getConfig();
	}

	static void release( @Disposes Config config )
	{
		ConfigProviderResolver.instance().releaseConfig( config );
	}
}
