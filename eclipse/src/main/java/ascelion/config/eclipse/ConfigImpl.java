
package ascelion.config.eclipse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.conv.Converters;
import ascelion.logging.LOG;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigImpl implements ConfigInternal
{

	static private final LOG L = LOG.get();

	private final Collection<ConfigSource> sources = new ArrayList<>();
	private final Converters cvs;

	ConfigImpl( Converters cvs )
	{
		this.cvs = cvs;
	}

	@Override
	public <T> T getValue( String propertyName, Class<T> propertyType )
	{
		final String value = getValue( propertyName );

		if( value == null ) {
			L.trace( "Configuration not found: %s", propertyName );

			throw new NoSuchElementException( "Configuration not found: " + propertyName );
		}

		return convert( value, propertyType );
	}

	@Override
	public <T> Optional<T> getOptionalValue( String propertyName, Class<T> propertyType )
	{
		final String value = getValue( propertyName );

		if( value == null ) {
			return empty();
		}

		return ofNullable( convert( value, propertyType ) );
	}

	@Override
	public Iterable<String> getPropertyNames()
	{
		return sources()
			.flatMap( c -> c.getPropertyNames().stream() )
			.collect( Collectors.toSet() );
	}

	@Override
	public Iterable<ConfigSource> getConfigSources()
	{
		return () -> sources().iterator();
	}

	@Override
	public String getValue( String propertyName )
	{
		return sources()
			.map( cs -> cs.getValue( propertyName ) )
			.filter( Objects::nonNull )
			.findFirst()
			.orElse( null );
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

	private Stream<ConfigSource> sources()
	{
		return this.sources.stream()
			.sorted( ( s1, s2 ) -> -Integer.compare( s1.getOrdinal(), s2.getOrdinal() ) );
	}
}
