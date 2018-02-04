
package ascelion.config.eclipse.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;

import ascelion.config.eclipse.ext.ConfigExt;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

class ConfigFactory
{

	@Produces
	@ApplicationScoped
	@Typed( { Config.class, ConfigExt.class } )
	static ConfigExt getConfigExt()
	{
		return ConfigExt.wrap( ConfigProvider.getConfig() );
	}

	static void release( @Disposes Config config )
	{
		ConfigProviderResolver.instance().releaseConfig( config );
	}
}
