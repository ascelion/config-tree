
package ascelion.config.impl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.eclipse.ConfigProviderResolver;
import ascelion.config.eclipse.References;

import static java.util.Arrays.asList;

public abstract class ConfigSources
{

	static private volatile ConfigSources instance = null;

	public static void setInstance( ConfigSources instance )
	{
		ConfigSources.instance = instance;
	}

	public static ConfigSources instance()
	{
		if( instance == null ) {
			synchronized( ConfigSources.class ) {
				if( instance != null ) {
					return instance;
				}

				ClassLoader cl = AccessController.doPrivileged( (PrivilegedAction<ClassLoader>) () -> Thread.currentThread().getContextClassLoader() );
				if( cl == null ) {
					cl = ConfigSources.class.getClassLoader();
				}

				final ConfigSources newInstance = loadSpi( cl );

				if( newInstance == null ) {
					throw new IllegalStateException( "No ConfigSources implementation found!" );
				}

				instance = newInstance;
			}
		}

		return instance;
	}

	private static ConfigSources loadSpi( ClassLoader cl )
	{
		if( cl == null ) {
			return null;
		}

		// start from the root CL and go back down to the TCCL
		ConfigSources instance = loadSpi( cl.getParent() );

		if( instance == null ) {
			final ServiceLoader<ConfigSources> sl = ServiceLoader.load(
				ConfigSources.class, cl );
			for( final ConfigSources spi : sl ) {
				if( instance != null ) {
					throw new IllegalStateException( "Multiple ConfigLoad implementations found: "
						+ spi.getClass().getName() + " and "
						+ instance.getClass().getName() );
				}
				else {
					instance = spi;
				}
			}
		}
		return instance;
	}

	private final References<Iterable<ConfigSource>> sources = new References<>();
	private final References<Iterable<ConfigReader>> READERS = new References<>();

	private final Collection<ConfigSource> addedSources = new HashSet<>();
	private final Collection<Predicate<ConfigSource>> sourceFilter = new ArrayList<>();
	private final Collection<ConfigReader> addedReaders = new ArrayList<>();
	private final Collection<Predicate<ConfigReader>> readerFilter = new ArrayList<>();

	public final void addSources( ConfigSource... sources )
	{
		this.addedSources.addAll( asList( sources ) );
	}

	public Iterable<ConfigSource> getSources( ClassLoader cld )
	{
		cld = ConfigProviderResolver.classLoader( cld );

		return this.sources.get( cld, this::buildSources );
	}

	public final void addSourceFilter( Predicate<ConfigSource> csf )
	{
		this.sourceFilter.add( csf );
	}

	public final void addReaders( ConfigReader... readers )
	{
		this.addedReaders.addAll( asList( readers ) );
	}

	public Iterable<ConfigReader> getReaders( ClassLoader cld )
	{
		cld = ConfigProviderResolver.classLoader( cld );

		return this.READERS.get( cld, this::buildReaders );
	}

	public final void addReaderFilter( Predicate<ConfigReader> rdf )
	{
		this.readerFilter.add( rdf );
	}

	protected abstract Iterable<ConfigSource> loadSources( ClassLoader cld );

	protected Iterable<ConfigReader> loadReaders( ClassLoader cld )
	{
		return ServiceLoader.load( ConfigReader.class, cld );
	}

	private Iterable<ConfigSource> buildSources( ClassLoader cld )
	{
		final Collection<ConfigSource> sources = new ArrayList<>();

		sources.addAll( this.addedSources );
		loadSources( cld ).forEach( sources::add );

		return () -> sources.stream().filter( this::accept ).iterator();
	}

	private boolean accept( ConfigSource cs )
	{
		if( this.sourceFilter.isEmpty() ) {
			return true;
		}

		return this.sourceFilter.stream().anyMatch( p -> p.test( cs ) );
	}

	private Iterable<ConfigReader> buildReaders( ClassLoader cld )
	{
		final Collection<ConfigReader> readers = new ArrayList<>();

		readers.addAll( this.addedReaders );
		loadReaders( cld ).forEach( readers::add );

		return () -> readers.stream().filter( this::accept ).iterator();
	}

	private boolean accept( ConfigReader rd )
	{
		if( this.readerFilter.isEmpty() ) {
			return true;
		}

		return this.readerFilter.stream().anyMatch( p -> p.test( rd ) );
	}

}
