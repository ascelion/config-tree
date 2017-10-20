
package ascelion.shared.cdi.conf;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
class ConfigCollect
{

	@Vetoed
	static class ConfigNodeTA extends TypeAdapter<ConfigNode>
	{

		@Override
		public void write( JsonWriter out, ConfigNode value ) throws IOException
		{
			final String item = value.getItem();
			final Map<String, ConfigNode> tree = value.getTree();

			if( tree != null ) {
				out.beginObject();

				if( item != null ) {
					out.name( "@" ).value( item );
				}
				for( final Map.Entry<String, ConfigNode> e : tree.entrySet() ) {
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
		public ConfigNode read( JsonReader in ) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	static private final Logger L = LoggerFactory.getLogger( ConfigCollect.class );

	@Inject
	private BeanManager bm;

	@Inject
	private ConfigExtension ext;

	private final ConfigNode root = new ConfigNode();

	public ConfigNode getRoot()
	{
		return this.root;
	}

	@PostConstruct
	private void postConstruct()
	{
		this.ext.sources().forEach( s -> {
			final String f = s.value();
			final String t = getType( s );

			L.trace( "Source: type: {}, value: {}", t, f );

			final Bean<ConfigReader> rdb = getReader( t );
			final CreationalContext<ConfigReader> cc = this.bm.createCreationalContext( rdb );
			final ConfigReader rd = (ConfigReader) this.bm.getReference( rdb, ConfigReader.class, cc );

			try {
				rd.readConfiguration( this.root, f );
			}
			catch( final IOException e ) {
				throw new RuntimeException( f, e );
			}
			catch( final UnsupportedOperationException x1 ) {
				readFromURL( f, rd );
			}

			rdb.destroy( rd, cc );
		} );

		System.getProperties().forEach( ( k, v ) -> {
			this.root.set( (String) k, (String) v );
		} );

		if( L.isTraceEnabled() ) {
			final String s = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter( ConfigNode.class, new ConfigNodeTA() )
				.create()
				.toJson( this.root );

			L.trace( "Config: {}", s );
		}
	}

	private void readFromURL( final String f, final ConfigReader rd )
	{
		for( final Enumeration<URL> e = getURL( f ); e.hasMoreElements(); ) {
			final URL u = e.nextElement();

			try {
				rd.readConfiguration( this.root, u );
			}
			catch( final IOException x ) {
				throw new RuntimeException( u.toExternalForm(), x );
			}
		}
	}

	private Bean<ConfigReader> getReader( String t )
	{
		return (Bean<ConfigReader>) this.bm.getBeans( ConfigReader.class, new AnyLiteral() ).stream()
			.filter( b -> b.getBeanClass().isAnnotationPresent( ConfigSource.Type.class ) )
			.filter( b -> matches( b.getBeanClass().getAnnotation( ConfigSource.Type.class ), t ) )
			.findFirst()
			.orElseThrow( () -> new UnsatisfiedResolutionException( "Cannot find reader for type " + t ) );
	}

	private boolean matches( ConfigSource.Type t1, String t )
	{
		return Stream.concat( Stream.of( t1.value() ), Stream.of( t1.types() ) )
			.anyMatch( x -> t.equalsIgnoreCase( x ) );
	}

	private String getType( final ConfigSource s )
	{
		String t = s.type();

		if( isBlank( t ) ) {
			t = FilenameUtils.getExtension( s.value() );

			if( isBlank( t ) ) {
				throw new RuntimeException( format( "No type specified for configuration source %s", s.value() ) );
			}
		}

		return t;
	}

	private Enumeration<URL> getURL( String source )
	{
		final File file = new File( source );

		try {
			if( file.exists() ) {
				return enumeration( asList( file.toURI().toURL() ) );
			}

			return Thread.currentThread().getContextClassLoader().getResources( source );
		}
		catch( final IOException e ) {
			throw new RuntimeException( source, e );
		}
	}
}
