
package ascelion.config.eclipse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.ofNullable;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

public class ConfigImpl implements Config
{

	private final Collection<ConfigSource> sources = new ArrayList<>();

	@Override
	public <T> T getValue( String propertyName, Class<T> propertyType )
	{
		return null;
	}

	@Override
	public <T> Optional<T> getOptionalValue( String propertyName, Class<T> propertyType )
	{
		return ofNullable( getValue( propertyName, propertyType ) );
	}

	@Override
	public Iterable<String> getPropertyNames()
	{
		return this.sources.stream().flatMap( c -> c.getPropertyNames().stream() ).collect( Collectors.toSet() );
	}

	@Override
	public Iterable<ConfigSource> getConfigSources()
	{
		return unmodifiableCollection( this.sources );
	}

	void add( ConfigSource cs )
	{
		this.sources.add( cs );
	}

}
