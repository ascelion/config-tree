
package ascelion.config.eclipse.cdi;

import java.util.Optional;

import ascelion.config.api.ConvertersRegistry;
import ascelion.config.eclipse.AbstractConfig;
import ascelion.config.eclipse.ext.ConfigExt;
import ascelion.config.utils.ServiceInstance;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigWrapper extends AbstractConfig
{

	static private final ServiceInstance<ConvertersRegistry> si = new ServiceInstance<>( ConvertersRegistry.class );

	static ConfigExt wrap( Config cf )
	{
		return cf instanceof ConfigExt ? (ConfigExt) cf : new ConfigWrapper( cf );
	}

	final Config delegate;

	private ConfigWrapper( Config config )
	{
		super( si.get() );

		this.delegate = config;
	}

	@Override
	public <T> T getValue( String propertyName, Class<T> propertyType )
	{
		return this.delegate.getValue( propertyName, propertyType );
	}

	@Override
	public <T> Optional<T> getOptionalValue( String propertyName, Class<T> propertyType )
	{
		return this.delegate.getOptionalValue( propertyName, propertyType );
	}

	@Override
	public Iterable<String> getPropertyNames()
	{
		return this.delegate.getPropertyNames();
	}

	@Override
	public Iterable<ConfigSource> getConfigSources()
	{
		return this.delegate.getConfigSources();
	}

}
