
package ascelion.config.eclipse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import ascelion.config.conv.Converters;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.ofNullable;

import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigImpl implements ConfigInternal
{

	private final Collection<ConfigSource> sources = new ArrayList<>();
	private final Converters cvs;

	ConfigImpl( Converters cvs )
	{
		this.cvs = cvs;
	}

	@Override
	public <T> T getValue( String propertyName, Class<T> propertyType )
	{
		return convert( getValue( propertyName ), propertyType );
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

	@Override
	public String getValue( String propertyName )
	{
		for( final ConfigSource cs : this.sources ) {
			final String value = cs.getValue( propertyName );

			if( value != null ) {
				return value;
			}
		}

		return null;
	}

	@Override
	public <T> T convert( String value, Type type )
	{
		try {
			return (T) this.cvs.create( type, value );
		}
		catch( final IllegalArgumentException e ) {
			throw e;
		}
		catch( final RuntimeException e ) {
			throw new IllegalArgumentException( value, e );
		}
	}

	void addSources( Iterable<? extends ConfigSource> sources )
	{
		sources.forEach( this.sources::add );
	}

	void addSource( ConfigSource source )
	{
		this.sources.add( source );
	}

}
