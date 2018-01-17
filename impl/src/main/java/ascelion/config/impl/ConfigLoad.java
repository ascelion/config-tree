
package ascelion.config.impl;

import java.io.IOException;
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
import ascelion.logging.LOG;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FilenameUtils;

public final class ConfigLoad
{

	static class ConfigNodeTA extends TypeAdapter<ConfigNodeImpl>
	{

		@Override
		public void write( JsonWriter out, ConfigNodeImpl node ) throws IOException
		{
			final String expression = node.getExpression();
			final Collection<ConfigNode> tree = node.getNodes();

			if( expression != null ) {
				out.name( "expression" ).value( expression );
			}
			if( tree.size() > 0 ) {
				out.beginObject();
				for( final ConfigNode child : tree ) {
					out.name( child.getName() );
					write( out, (ConfigNodeImpl) child );
				}
				out.endObject();
			}
		}

		@Override
		public ConfigNodeImpl read( JsonReader in ) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	static private final LOG L = LOG.get();

	private final Map<String, ConfigReader> readers = new TreeMap<>();
	private final Collection<ConfigSource> sources = new ArrayList<>();

	public void addReader( ConfigReader rd )
	{
		final Class<? extends ConfigReader> c = rd.getClass();

		final ConfigReader.Type t = Utils.findAnnotation( ConfigReader.Type.class, c )
			.orElseThrow( () -> new ConfigException( format( "Cannot find annotation @ConfigReader.Type on class %s", c.getName() ) ) );

		L.trace( "Adding reader %s from %s", t.value(), c.getSimpleName() );

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
				.registerTypeHierarchyAdapter( ConfigNode.class, new ConfigNodeTA() )
				.create()
				.toJson( root );

			L.trace( "Config: %s", s );
		}

		return root;
	}

	private void load( ConfigSource source, ConfigNodeImpl root )
	{
		final String type = getType( source );
		final ConfigReader rd = getReader( type );

		if( rd.enabled() ) {
			try {
				L.trace( "Reading: type %s from '%s'", type, source.value() );

				root.setValue( rd.readConfiguration( source ) );
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
			L.warn( "Cannot find configuration source %s", source.value() );
		}
		else {
			for( final URL u : all ) {
				L.trace( "Reading: type %s from '%s'", type, u );

				try {
					root.setValue( rd.readConfiguration( source, u ) );
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
