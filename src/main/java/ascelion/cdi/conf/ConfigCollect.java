
package ascelion.cdi.conf;

import java.io.IOException;
import java.net.URL;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

import static java.lang.String.format;
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

	static private final Logger L = LoggerFactory.getLogger( ConfigCollect.class );

	@Inject
	private BeanManager bm;

	@Inject
	private ConfigExtension ext;

	private final ConfigNodeImpl root = new ConfigNodeImpl();

	private final Map<ConfigReader, InstanceInfo<ConfigReader>> readers = new IdentityHashMap<>();

	@Produces
	@Dependent
	@Typed( ConfigNode.class )
	synchronized ConfigNodeImpl root()
	{
		return this.root;
	}

	synchronized void readConfiguration( @Observes ConfigSource source )
	{
		final String t = getType( source );
		final ConfigReader rd = getReader( t );

		if( rd.enabled() ) {
			try {
				L.trace( "Reading: type {} from '{}'", t, source.value() );

				rd.readConfiguration( source, this.root );
			}
			catch( final UnsupportedOperationException x1 ) {
				readFromURL( source, t, rd );
			}
		}
	}

	@PostConstruct
	private void postConstruct()
	{
		this.bm.getBeans( ConfigReader.class, new AnyLiteral() )
			.stream().map( b -> (Bean<ConfigReader>) b )
			.filter( b -> b.getBeanClass().isAnnotationPresent( ConfigSource.Type.class ) )
			.forEach( b -> {
				final InstanceInfo<ConfigReader> info = new InstanceInfo<>( this.bm, b );

				this.readers.put( info.instance, info );
			} );

		this.ext.sources().forEach( s -> {
			readConfiguration( s );
		} );

		if( L.isTraceEnabled() ) {
			final String s = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter( ConfigNodeImpl.class, new ConfigNodeTA() )
				.create()
				.toJson( this.root );

			L.trace( "Config: {}", s );
		}
	}

	@PreDestroy
	private void preDestroy()
	{
		this.readers.forEach( ( rd, tup ) -> {
			tup.bean.destroy( rd, tup.context );
		} );

		this.readers.clear();
	}

	private void readFromURL( ConfigSource source, String t, ConfigReader rd )
	{
		final List<URL> all = ConfigReader.getResources( source.value() );

		if( all.isEmpty() ) {
			L.warn( "Cannot find configuration source {}", source.value() );
		}
		else {
			all.forEach( u -> {
				L.trace( "Reading: type {} from '{}'", t, u );

				rd.readConfiguration( source, this.root, u );
			} );
		}
	}

	private ConfigReader getReader( String t )
	{
		return this.readers.entrySet().stream()
			.filter( e -> matches( e.getValue().qualifier( ConfigSource.Type.class ), t ) )
			.map( Map.Entry::getKey )
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
}
