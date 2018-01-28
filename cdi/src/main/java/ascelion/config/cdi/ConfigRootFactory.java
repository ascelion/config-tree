
package ascelion.config.cdi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRegistry;

class ConfigRootFactory
{

	@Produces
	@ApplicationScoped
	private ConfigNode root;

	@PostConstruct
	private void postConstruct()
	{
		this.root = ConfigRegistry.getInstance( getClass().getClassLoader() ).root();
	}
}
