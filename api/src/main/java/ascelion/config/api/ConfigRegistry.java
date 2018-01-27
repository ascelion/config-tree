
package ascelion.config.api;

import java.util.function.Predicate;

import ascelion.config.utils.Iterables;
import ascelion.config.utils.ServiceInstance;

public abstract class ConfigRegistry
{

	static private final ServiceInstance<ConfigRegistry> si = new ServiceInstance<>( ConfigRegistry.class );

	static public ConfigRegistry getInstance()
	{
		return si.get();
	}

	static public void setInstance( ConfigRegistry instance )
	{
		si.set( instance );
	}

	private final Iterables<ConfigSource> sources = new Iterables<>();
	private final Iterables<ConfigReader> readers = new Iterables<>();

	// sources
	public final void add( ConfigSource... objects )
	{
		this.sources.add( objects );
	}

	public final void addSources( Iterable<ConfigSource> objects )
	{
		this.sources.add( objects );
	}

	public final void filterSource( Predicate<ConfigSource> f )
	{
		this.sources.filter( f );
	}

	public final Iterable<ConfigSource> getSources( ClassLoader cld )
	{
		return this.sources.get( cld, this::loadSources );
	}

	protected abstract Iterable<ConfigSource> loadSources( ClassLoader cld );

	// readers
	public final void add( ConfigReader... objects )
	{
		this.readers.add( objects );
	}

	public final void addReaders( Iterable<ConfigReader> objects )
	{
		this.readers.add( objects );
	}

	public final void filterReader( Predicate<ConfigReader> f )
	{
		this.readers.filter( f );
	}

	public final Iterable<ConfigReader> getReaders( ClassLoader cld )
	{
		return this.readers.get( cld, this::loadReaders );
	}

	protected abstract Iterable<ConfigReader> loadReaders( ClassLoader cld );

	protected abstract Iterable<ConfigConverter<?>> loadConverters( ClassLoader cld );
}
