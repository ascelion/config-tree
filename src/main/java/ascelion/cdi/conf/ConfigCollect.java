
package ascelion.cdi.conf;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
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
import static java.util.Collections.list;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
class ConfigCollect
{

	static class DelayedSource implements Delayed
	{

		final ConfigSource source;
		private final long next;

		DelayedSource( ConfigSource source, long await )
		{
			this.source = source;
			this.next = System.nanoTime() + await;
		}

		@Override
		public int compareTo( Delayed o )
		{
			final DelayedSource that = (DelayedSource) o;
			int c;

			if( ( c = Long.compare( this.next, that.next ) ) != 0 ) {
				return c;
			}

			if( ( c = Integer.compare( this.source.priority(), that.source.priority() ) ) != 0 ) {
				return c;
			}

			if( ( c = this.source.type().compareTo( that.source.type() ) ) != 0 ) {
				return c;
			}

			if( ( c = this.source.value().compareTo( that.source.value() ) ) != 0 ) {
				return c;
			}

			return 0;
		}

		@Override
		public long getDelay( TimeUnit unit )
		{
			return unit.convert( this.next - System.nanoTime(), NANOSECONDS );
		}
	}

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

	private final DelayQueue<DelayedSource> sources = new DelayQueue<>();

	@Produces
	@Dependent
	@Typed( ConfigNode.class )
	ConfigNodeImpl root()
	{
		readConfigurations();

		return this.root;
	}

	private synchronized void readConfigurations()
	{
		final Collection<Pair<ConfigSource, ConfigSource.Reload>> used = new ArrayList<>();
		DelayedSource item;

		while( ( item = this.sources.poll() ) != null ) {
			final ConfigSource.Reload reload = readConfiguration( item.source );

			used.add( new ImmutablePair<>( item.source, reload ) );
		}

		if( L.isTraceEnabled() ) {
			final String s = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter( ConfigNodeImpl.class, new ConfigNodeTA() )
				.create()
				.toJson( this.root );

			L.trace( "Config: {}", s );
		}

		used.forEach( i -> {
			if( !addNext( i.getLeft(), i.getLeft().reload() ) ) {
				addNext( i.getLeft(), i.getRight() );
			}
		} );
	}

	private ConfigSource.Reload readConfiguration( ConfigSource source )
	{
		final String t = getType( source );
		final String f = source.value();
		final ConfigReader rd = getReader( t );

		try {
			L.trace( "Reading: type {} from '{}'", t, f );

			rd.readConfiguration( this.root, f );
		}
		catch( final UnsupportedOperationException x1 ) {
			readFromURL( f, t, rd );
		}

		return this.readers.get( rd ).qualifier( ConfigSource.Type.class ).reload();
	}

	private boolean addNext( ConfigSource source, ConfigSource.Reload reload )
	{
		if( reload.value() >= 0 ) {
			final long next = reload.unit().toNanos( reload.value() );

			L.trace( "Reload: type {} from '{}' within {} seconds", source.type(), source.value(), NANOSECONDS.toSeconds( next ) );

			this.sources.add( new DelayedSource( source, next ) );

			return true;
		}

		return false;
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
			this.sources.add( new DelayedSource( s, 0 ) );
		} );

		this.ext.properties().forEach( s -> {
			this.root.set( s, null );
		} );

		readConfigurations();
	}

	@PreDestroy
	private void preDestroy()
	{
		this.readers.forEach( ( rd, tup ) -> {
			tup.bean.destroy( rd, tup.context );
		} );

		this.readers.clear();
		this.sources.clear();
	}

	private void readFromURL( final String f, String t, final ConfigReader rd )
	{
		final List<URL> all = getAll( f );

		if( all.isEmpty() ) {
			L.warn( "Cannot find configuration source {}", f );
		}
		else {
			all.forEach( u -> {
				L.trace( "Reading: type {} from '{}'", t, u );

				rd.readConfiguration( this.root, u );
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

	private List<URL> getAll( String source )
	{
		final List<URL> all = new ArrayList<>();
		final File file = new File( source );

		try {
			all.addAll( list( Thread.currentThread().getContextClassLoader().getResources( source ) ) );

			if( file.exists() ) {
				all.add( file.toURI().toURL() );
			}

			return all;
		}
		catch( final IOException e ) {
			throw new RuntimeException( source, e );
		}
	}
}
