
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

	static public ConfigRegistry getInstance( ClassLoader cld )
	{
		return si.get( cld );
	}

	static public void setInstance( ClassLoader cld, ConfigRegistry instance )
	{
		si.set( cld, instance );
	}

	private final Iterables<ConfigSource> sources = new Iterables<>();
	private final Iterables<ConfigReader> readers = new Iterables<>();
	private final ServiceInstance<ConvertersRegistry> cvs = new ServiceInstance<>( ConvertersRegistry.class );

	@ServiceInstance.CLD
	private ClassLoader cld;

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

	public final Iterable<ConfigSource> getSources()
	{
		return this.sources.get( () -> loadSources( this.cld ) );
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

	public final Iterable<ConfigReader> getReaders()
	{
		return this.readers.get( () -> loadReaders( this.cld ) );
	}

	protected abstract Iterable<ConfigReader> loadReaders( ClassLoader cld );

	// converters
	public final ConvertersRegistry converters()
	{
		return this.cvs.get();
	}

	// root node
	public final ConfigNode root()
	{
		return load( this.cld );
	}

	protected abstract ConfigNode load( ClassLoader cld );
}
