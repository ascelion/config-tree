
package ascelion.config.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigLoad
{

	static private final Logger L = LoggerFactory.getLogger( ConfigLoad.class );

	private final Map<String, ConfigReader> readers = new TreeMap<>();
	private final Collection<ConfigSource> sources = new ArrayList<>();

	public void addReader( ConfigReader rd )
	{
		final Class<? extends ConfigReader> c = rd.getClass();

		final ConfigReader.Type t = Utils.findAnnotation( ConfigReader.Type.class, c )
			.orElseThrow( () -> new ConfigException( format( "Cannot find annotation @ConfigReader.Type on class %s", c.getName() ) ) );

		L.trace( format( "Adding reader %s from %s", t.value(), c.getSimpleName() ) );

		this.readers.put( t.value().toUpperCase(), rd );

		Stream.of( t.types() ).forEach( x -> this.readers.put( x.toUpperCase(), rd ) );
	}

	public void addReaders( Collection<? extends ConfigReader> readers )
	{
		readers.forEach( this::addReader );
	}

	public void addSource( ConfigSource source )
	{
		this.sources.add( source );
	}

	public void addSources( Collection<ConfigSource> sources )
	{
		this.sources.addAll( sources );
	}

	public ConfigNode load()
	{
		final ConfigNodeImpl root = new ConfigNodeImpl();

		this.sources.stream()
			.sorted( ( s1, s2 ) -> Integer.compare( s1.priority(), s2.priority() ) )
			.forEach( s -> {
				load( s, root );
			} );

		if( L.isTraceEnabled() ) {
			final String s = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeHierarchyAdapter( ConfigNode.class, new ConfigNodeImpl.ConfigNodeTA() )
				.create()
				.toJson( root );

			L.trace( "Config: {}", s );
		}

		return root;
	}

	private void load( ConfigSource source, ConfigNodeImpl root )
	{
		final String type = getType( source );
		final ConfigReader rd = getReader( type );

		if( rd.enabled() ) {
			try {
				L.trace( "Reading: type {} from '{}'", type, source.value() );

				root.set( rd.readConfiguration( source ) );
			}
			catch( final UnsupportedOperationException x ) {
				readFromURL( source, type, rd, root );
			}
			catch( final ConfigException e ) {
				L.error( "Cannot read config source: " + source.value() );

				throw e;
			}
		}
	}

	private void readFromURL( ConfigSource source, String type, ConfigReader rd, ConfigNodeImpl root )
	{
		final List<URL> all = ConfigReader.getResources( source.value() );

		if( all.isEmpty() ) {
			L.warn( "Cannot find configuration source {}", source.value() );
		}
		else {
			for( final URL u : all ) {
				L.trace( "Reading: type {} from '{}'", type, u );

				try {
					root.set( rd.readConfiguration( source, u ) );
				}
				catch( final ConfigException e ) {
					L.error( "Cannot read config source: " + source.value() );

					throw e;
				}
			}
			;
		}
	}

	private String getType( final ConfigSource source )
	{
		String t = source.type();

		if( isBlank( t ) ) {
			t = FilenameUtils.getExtension( source.value() );

			if( isBlank( t ) ) {
				throw new ConfigException( format( "No type specified for configuration source %s", source.value() ) );
			}
		}

		return t.toUpperCase();
	}

	private ConfigReader getReader( String type )
	{
		final ConfigReader r = this.readers.get( type );

		if( r == null ) {
			throw new ConfigException( format( "Cannot find any reader for configuration type %s", type ) );
		}

		return r;
	}
}
