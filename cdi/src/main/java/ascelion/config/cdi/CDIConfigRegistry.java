
package ascelion.config.cdi;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Singleton;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.DefaultConfigRegistry;

@Singleton
@Typed( ConfigRegistry.class )
class CDIConfigRegistry extends DefaultConfigRegistry
{

	@Inject
	@Any
	private Instance<ConfigReader> readers;

	@Inject
	private ConfigExtension ext;

	@Override
	protected Iterable<ConfigReader> loadReaders( ClassLoader cld )
	{
		return this.readers;
	}

	@Override
	protected Iterable<ConfigSource> loadSources( ClassLoader cld )
	{
		return this.ext.sources();
	}

}
