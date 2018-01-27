
package ascelion.config.cdi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import ascelion.config.api.ConfigNode;
import ascelion.config.impl.ConfigLoad;

class ConfigNodeFactory
{

	@Produces
	@ApplicationScoped
	private ConfigNode root;

	@PostConstruct
	private void postConstruct()
	{
		this.root = new ConfigLoad().load();
	}
}
