
package ascelion.config.eclipse.cdi;

import java.lang.reflect.Type;
import java.util.Optional;

import ascelion.config.conv.Converters;
import ascelion.config.eclipse.ConfigInternal;
import ascelion.config.eclipse.ConverterReg;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigWrapper implements ConfigInternal
{

	static ConfigInternal wrap( Config cf )
	{
		return cf instanceof ConfigInternal ? (ConfigInternal) cf : new ConfigWrapper( cf );
	}

	final Config delegate;
	private final Converters cvs;

	private ConfigWrapper( Config config )
	{
		this.delegate = config;
		this.cvs = new ConverterReg().discover( null ).get();
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

	@Override
	public String getValue( String propertyName )
	{
		return getValue( propertyName, String.class );
	}

	@Override
	public <T> T convert( String value, Type type )
	{
		return (T) this.cvs.create( type, value );
	}
}
