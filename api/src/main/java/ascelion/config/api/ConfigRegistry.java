
package ascelion.config.api;

import ascelion.config.utils.ServiceInstance;

public abstract class ConfigRegistry
{

	static private final ServiceInstance<ConfigRegistry> si = new ServiceInstance<>( ConfigRegistry.class );

	static public ConfigRegistry getSi()
	{
		return si.get();
	}

	static public void setInstance( ConfigRegistry instance )
	{
		si.set( instance );
	}

	protected abstract Iterable<ConfigSource> loadSources( ClassLoader cld );

	protected abstract Iterable<ConfigReader> loadReaders( ClassLoader cld );

	protected abstract Iterable<ConfigConverter<?>> loadConverters( ClassLoader cld );
}
