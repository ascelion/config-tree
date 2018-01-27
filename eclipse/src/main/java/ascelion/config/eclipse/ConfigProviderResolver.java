
package ascelion.config.eclipse;

import java.util.Objects;

import ascelion.config.utils.References;
import ascelion.config.utils.ServiceInstance;

import org.eclipse.microprofile.config.Config;

public final class ConfigProviderResolver extends org.eclipse.microprofile.config.spi.ConfigProviderResolver
{

	private final References<Config> configs = new References<>();

	@Override
	public Config getConfig()
	{
		return getConfig( ServiceInstance.classLoader( null, getClass() ) );
	}

	@Override
	public Config getConfig( ClassLoader cld )
	{
		Objects.requireNonNull( cld, "The classLoader cannot be null" );

		return this.configs.get( cld, this::buildConfig );
	}

	@Override
	public ConfigBuilderImpl getBuilder()
	{
		return new ConfigBuilderImpl();
	}

	@Override
	public void registerConfig( Config config, ClassLoader cld )
	{
		Objects.requireNonNull( cld, "The classLoader cannot be null" );

		this.configs.put( cld, config );
	}

	@Override
	public void releaseConfig( Config config )
	{
		this.configs.remove( config );
	}

	private Config buildConfig( ClassLoader cld )
	{
		return getBuilder()
			.addDefaultSources()
			.addDiscoveredSources()
			.addDiscoveredConverters()
			.forClassLoader( cld )
			.build();
	}
}
