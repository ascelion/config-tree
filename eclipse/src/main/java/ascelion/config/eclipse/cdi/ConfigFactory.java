
package ascelion.config.eclipse.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import ascelion.config.eclipse.ext.ConfigExt;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

class ConfigFactory
{

	@Produces
	@ApplicationScoped
	static ConfigExt getConfig()
	{
		return ConfigWrapper.wrap( ConfigProviderResolver.instance().getConfig() );
	}

	static void release( @Disposes Config config )
	{
		if( config instanceof ConfigWrapper ) {
			config = ( (ConfigWrapper) config ).delegate;
		}

		ConfigProviderResolver.instance().releaseConfig( config );
	}
}
