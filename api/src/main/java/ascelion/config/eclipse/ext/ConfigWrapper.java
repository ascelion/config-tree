
package ascelion.config.eclipse.ext;

import java.util.Optional;

import ascelion.config.api.ConvertersRegistry;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigWrapper extends AbstractConfig
{

	final Config delegate;

	ConfigWrapper( Config config )
	{
		super( ConvertersRegistry.getInstance() );

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
