
package ascelion.shared.cdi.conf;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import ascelion.shared.cdi.conf.ConfigSource.Type;

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

	static class ReaderInfo
	{

		final Bean<ConfigReader> bean;
		final CreationalContext<ConfigReader> context;
		final Type type;

		ReaderInfo( Bean<ConfigReader> bean, CreationalContext<ConfigReader> context, ConfigSource.Type type )
		{
			this.bean = bean;
			this.context = context;
			this.type = type;
		}
	}

	static class ConfigInfo implements Comparable<ConfigInfo>
	{

		final ConfigSource source;
		long timeout;

		public ConfigInfo( ConfigSource source, long timeout )
		{
			this.source = source;
			this.timeout = timeout;
		}

		@Override
		public int compareTo( ConfigInfo that )
		{
			if( this.timeout != that.timeout ) {
				return Long.compare( this.timeout, that.timeout );
			}
			if( this.source.priority() != that.source.priority() ) {
				return Integer.compare( this.source.priority(), that.source.priority() );
			}

			return this.source.value().compareTo( that.source.value() );
		}
	}

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

	private final Map<ConfigReader, ReaderInfo> readers = new IdentityHashMap<>();

	private final Set<ConfigInfo> sources = new TreeSet<>();

	public ConfigNode getRoot()
	{
		readConfigurations();

		return this.root;
	}

	private synchronized void readConfigurations()
	{
		final List<ConfigInfo> toRead = new ArrayList<>( this.sources );

		this.sources.clear();

		toRead.forEach( i -> {
			if( i.timeout < System.currentTimeMillis() ) {
				readConfiguration( i.source );
			}
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

	private void readConfiguration( ConfigSource source )
	{
		final String t = getType( source );
		final String f = source.value();
		final ConfigReader rd = getReader( t );

		try {
			rd.readConfiguration( this.root, f );
		}
		catch( final IOException e ) {
			throw new RuntimeException( f, e );
		}
		catch( final UnsupportedOperationException x1 ) {
			readFromURL( f, rd );
		}

		if( source.reload().value() >= 0 ) {
			final long next = System.currentTimeMillis() + source.reload().unit().toMillis( source.reload().value() );

			this.sources.add( new ConfigInfo( source, next ) );
		}

	}

	@PostConstruct
	private void postConstruct()
	{
		this.bm.getBeans( ConfigReader.class, new AnyLiteral() )
			.stream().map( b -> (Bean<ConfigReader>) b )
			.filter( b -> b.getBeanClass().isAnnotationPresent( ConfigSource.Type.class ) )
			.forEach( b -> {
				final CreationalContext<ConfigReader> cc = this.bm.createCreationalContext( b );
				final ConfigReader rd = (ConfigReader) this.bm.getReference( b, ConfigReader.class, cc );
				final ReaderInfo info = new ReaderInfo( b, cc, b.getBeanClass().getAnnotation( ConfigSource.Type.class ) );

				this.readers.put( rd, info );
			} );

		this.ext.sources().forEach( s -> {
			this.sources.add( new ConfigInfo( s, 0L ) );
		} );

		readConfigurations();

		System.getProperties().forEach( ( k, v ) -> {
			this.root.set( (String) k, (String) v );
		} );

	}

	@PreDestroy
	private void preDestroy()
	{
		this.readers.forEach( ( rd, tup ) -> {
			tup.bean.destroy( rd, tup.context );
		} );

		this.readers.clear();
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

	private ConfigReader getReader( String t )
	{
		return this.readers.entrySet().stream()
			.filter( e -> matches( e.getValue().type, t ) )
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
