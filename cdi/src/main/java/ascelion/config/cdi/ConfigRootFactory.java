
package ascelion.config.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRegistry;

class ConfigRootFactory
{

	@Produces
	@ApplicationScoped
	ConfigNode postConstruct()
	{
		return ConfigRegistry.getInstance( getClass().getClassLoader() ).root();
	}
}
