
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
		public void write( JsonWriter out, ConfigNodeImpl value ) throws IOException
		{
			final String item = value.getValue();
			final Map<String, ConfigNodeImpl> tree = value.tree( false );

			if( tree != null ) {
				out.beginObject();

				if( item != null ) {
					out.name( "@" ).value( item );
				}
				for( final Map.Entry<String, ConfigNodeImpl> e : tree.entrySet() ) {
					out.name( e.getKey() );
					write( out, e.getValue() );
				}

				out.endObject();
			}
			else {
				out.value( item );
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
		final ConfigNode root = new ConfigNodeImpl();

		sources.stream()
			.sorted( ( s1, s2 ) -> Integer.compare( s1.priority(), s2.priority() ) )
			.forEach( s -> load( s, root ) );

		if( L.isTraceEnabled() ) {
			final String s = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter( ConfigNodeImpl.class, new ConfigNodeTA() )
				.create()
				.toJson( root );

			L.trace( "Config: {}", s );
		}

		return root;
	}

	public void load( ConfigSource source, ConfigNode root )
	{
		final String t = getType( source );
		final ConfigReader r = getReader( t );

		if( r.enabled() ) {
			try {
				L.trace( "Reading: type {} from '{}'", t, source.value() );

				r.readConfiguration( source, root );
			}
			catch( final UnsupportedOperationException x ) {
				readFromURL( source, t, r, root );
			}
		}
	}

	private void readFromURL( ConfigSource source, String type, ConfigReader rd, ConfigNode root )
	{
		final List<URL> all = ConfigReader.getResources( source.value() );

		if( all.isEmpty() ) {
			L.warn( "Cannot find configuration source {}", source.value() );
		}
		else {
			all.forEach( u -> {
				L.trace( "Reading: type {} from '{}'", type, u );

				rd.readConfiguration( source, root, u );
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
