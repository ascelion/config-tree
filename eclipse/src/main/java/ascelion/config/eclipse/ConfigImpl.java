
package ascelion.config.eclipse;

import java.util.Optional;

import ascelion.config.api.ConfigNode;

import static java.util.Optional.ofNullable;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class ConfigImpl implements Config
{

	private final ConfigNode root;

	public ConfigImpl( ConfigNode root )
	{
		this.root = root;
	}

	@Override
	public <T> T getValue( String propertyName, Class<T> propertyType )
	{
		return (T) this.root.getValue();
	}

	@Override
	public <T> Optional<T> getOptionalValue( String propertyName, Class<T> propertyType )
	{
		return ofNullable( getValue( propertyName, propertyType ) );
	}

	@Override
	public Iterable<String> getPropertyNames()
	{
		return this.root.getKeys();
	}

	@Override
	public Iterable<ConfigSource> getConfigSources()
	{
		return null;
	}

}
