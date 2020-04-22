
package ascelion.config.cdi;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

class CDIConfigProvider
{

	@Produces
	@ApplicationScoped
	public ConfigRoot get()
	{
		return ConfigProvider.root();
	}
}
