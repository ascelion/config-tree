
package ascelion.config.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.DefaultConfigRegistry;

@Singleton
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

	void applicationInitialised( @Observes @Initialized( ApplicationScoped.class ) Object event )
	{
		ConfigRegistry.setInstance( this );
	}

	void applicationDestroyed( @Observes @Destroyed( ApplicationScoped.class ) Object event )
	{
		ConfigRegistry.setInstance( null );
	}

}
