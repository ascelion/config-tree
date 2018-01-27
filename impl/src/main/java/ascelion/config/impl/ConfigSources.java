
package ascelion.config.impl;

import java.util.ServiceLoader;
import java.util.function.Predicate;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.utils.Iterables;
import ascelion.config.utils.ServiceInstance;

public abstract class ConfigSources
{

	static private final ServiceInstance<ConfigSources> si = new ServiceInstance<>( ConfigSources.class, DefaultConfigSources::new );

	public static void setInstance( ConfigSources instance )
	{
		si.set( instance );
	}

	public static ConfigSources instance()
	{
		return si.get();
	}

	private final Iterables<ConfigSource> sources = new Iterables<>();
	private final Iterables<ConfigReader> readers = new Iterables<>();

	public final void addSources( ConfigSource... sources )
	{
		this.sources.add( sources );
	}

	public Iterable<ConfigSource> getSources( ClassLoader cld )
	{
		cld = ServiceInstance.classLoader( cld, getClass() );

		return this.sources.get( cld, this::loadSources );
	}

	public final void setSourceFilter( Predicate<ConfigSource> csf )
	{
		this.sources.filter( csf );
	}

	public final void addReaders( ConfigReader... readers )
	{
		this.readers.add( readers );
	}

	public Iterable<ConfigReader> getReaders( ClassLoader cld )
	{
		cld = ServiceInstance.classLoader( cld, getClass() );

		return this.readers.get( cld, this::loadReaders );
	}

	public final void setReaderFilter( Predicate<ConfigReader> rdf )
	{
		this.readers.filter( rdf );
	}

	protected abstract Iterable<ConfigSource> loadSources( ClassLoader cld );

	protected Iterable<ConfigReader> loadReaders( ClassLoader cld )
	{
		return ServiceLoader.load( ConfigReader.class, cld );
	}

}
