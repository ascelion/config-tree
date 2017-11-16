
package ascelion.config.impl;

import java.io.IOException;
import java.net.URL;
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
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoad
{

	static private final Logger L = LoggerFactory.getLogger( ConfigLoad.class );

	static class ConfigNodeTA extends TypeAdapter<ConfigNodeImpl>
	{

		@Override
		public void write( JsonWriter out, ConfigNodeImpl root ) throws IOException
		{
			final String value = root.getLiteral();
			final Map<String, ConfigNodeImpl> nodes = root.tree( false );

			if( nodes != null ) {
				out.beginObject();

				if( value != null ) {
					out.name( "@" ).value( value );
				}
				for( final ConfigNodeImpl node : nodes.values() ) {
					out.name( node.getName() );
					write( out, node );
				}

				out.endObject();
			}
			else {
				out.value( value );
			}
		}

		@Override
		public ConfigNodeImpl read( JsonReader in ) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	private final Map<String, ConfigReader> readers = new TreeMap<>();

	public void addReader( ConfigReader rd )
	{
		final Class<? extends ConfigReader> c = rd.getClass();

		final ConfigReader.Type t = Utils.findAnnotation( ConfigReader.Type.class, c )
			.orElseThrow( () -> new ConfigException( format( "Cannot find annotation @ConfigReader.Type on class %s", c.getName() ) ) );

		L.trace( format( "Adding reader %s from %s", t.value(), c.getSimpleName() ) );

		this.readers.put( t.value().toUpperCase(), rd );

		Stream.of( t.types() ).forEach( x -> this.readers.put( x.toUpperCase(), rd ) );
	}

	public void addReaders( Collection<ConfigReader> readers )
	{
		readers.forEach( this::addReader );
	}

	public ConfigNode load( Collection<ConfigSource> sources )
	{
		final ConfigNodeImpl root = new ConfigNodeImpl();

		sources.stream()
			.sorted( ( s1, s2 ) -> Integer.compare( s1.priority(), s2.priority() ) )
			.forEach( s -> {
				root.add( (ConfigNodeImpl) load( s ) );
			} );

		if( L.isTraceEnabled() ) {
			final String s = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeHierarchyAdapter( ConfigNode.class, new ConfigNodeTA() )
				.create()
				.toJson( root );

			L.trace( "Config: {}", s );
		}

		return root;
	}

	public ConfigNode load( ConfigSource source )
	{
		final ConfigNodeImpl root = new ConfigNodeImpl();
		final String t = getType( source );
		final ConfigReader r = getReader( t );

		if( r.enabled() ) {
			try {
				L.trace( "Reading: type {} from '{}'", t, source.value() );

				root.setValue( r.readConfiguration( source ) );
			}
			catch( final UnsupportedOperationException x ) {
				readFromURL( source, t, r, root );
			}
			catch( final ConfigException e ) {
				L.error( "Cannot read config source: " + source.value() );

				throw e;
			}
		}

		return root;
	}

	private void readFromURL( ConfigSource source, String type, ConfigReader rd, ConfigNodeImpl root )
	{
		final List<URL> all = ConfigReader.getResources( source.value() );

		if( all.isEmpty() ) {
			L.warn( "Cannot find configuration source {}", source.value() );
		}
		else {
			all.forEach( u -> {
				L.trace( "Reading: type {} from '{}'", type, u );

				try {
					root.setValue( rd.readConfiguration( source, u ) );
				}
				catch( final ConfigException e ) {
					L.error( "Cannot read config source: " + source.value() );

					throw e;
				}
			} );
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
