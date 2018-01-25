
package ascelion.config.eclipse;

import java.util.Objects;

import org.eclipse.microprofile.config.Config;

public final class ConfigProviderResolver extends org.eclipse.microprofile.config.spi.ConfigProviderResolver
{

	static ClassLoader classLoader( ClassLoader cld )
	{
		if( cld == null ) {
			cld = Thread.currentThread().getContextClassLoader();
		}
		if( cld == null ) {
			cld = ConfigProviderResolver.class.getClassLoader();
		}

		return cld;
	}

	static private final References<Config> CONFIGS = new References<>();

	@Override
	public Config getConfig()
	{
		return getConfig( classLoader( null ) );
	}

	@Override
	public Config getConfig( ClassLoader cld )
	{
		Objects.requireNonNull( cld, "The classLoader cannot be null" );

		return CONFIGS.get( cld, this::buildConfig );
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

		CONFIGS.put( cld, config );
	}

	@Override
	public void releaseConfig( Config config )
	{
		CONFIGS.remove( config );
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
