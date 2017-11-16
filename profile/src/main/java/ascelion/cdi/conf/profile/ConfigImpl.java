
package ascelion.cdi.conf.profile;

import java.util.Optional;

import ascelion.config.api.ConfigNode;
import ascelion.config.impl.ConfigJava;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class ConfigImpl implements Config
{

	private final ConfigJava java = new ConfigJava();
	private final ConfigNode root;

	public ConfigImpl( ConfigNode root )
	{
		this.root = root;
	}

	@Override
	public <T> T getValue( String propertyName, Class<T> propertyType )
	{
		return (T) this.root.getNode( propertyName ).getValue( true );
	}

	@Override
	public <T> Optional<T> getOptionalValue( String propertyName, Class<T> propertyType )
	{
		return Optional.ofNullable( getValue( propertyName, propertyType ) );
	}

	@Override
	public Iterable<String> getPropertyNames()
	{
		return this.root.asMap().keySet();
	}

	@Override
	public Iterable<ConfigSource> getConfigSources()
	{
		return null;
	}

}
