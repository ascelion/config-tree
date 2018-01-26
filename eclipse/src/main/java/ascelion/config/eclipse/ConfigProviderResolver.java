
package ascelion.config.eclipse;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

import org.eclipse.microprofile.config.Config;

public final class ConfigProviderResolver extends org.eclipse.microprofile.config.spi.ConfigProviderResolver
{

	static public ClassLoader classLoader( ClassLoader cld )
	{
		if( cld == null ) {
			cld = AccessController.doPrivileged( (PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader() );
		}
		if( cld == null ) {
			cld = ConfigProviderResolver.class.getClassLoader();
		}

		return cld;
	}

	private final References<Config> configs = new References<>();

	@Override
	public Config getConfig()
	{
		return getConfig( classLoader( null ) );
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
