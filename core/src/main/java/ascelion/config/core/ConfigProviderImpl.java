
package ascelion.config.core;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.convert.Converters;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInputReader;
import ascelion.config.spi.ConverterFactory;

import java.io.File;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ConfigProviderImpl extends ConfigProvider
{

	private static final Map<ClassLoader, ConfigRootImpl> ROOTS = new IdentityHashMap<>();
	private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();

	public static void reset()
	{
		final Lock lock = RW_LOCK.writeLock();

		lock.lock();

		try {
			ROOTS.clear();
		}
		finally {
			lock.unlock();
		}
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public ConfigRoot get( ClassLoader cld )
	{
		Lock lock = RW_LOCK.readLock();

		lock.lock();

		try {
			ConfigRootImpl root = ROOTS.get( cld );

			if( root != null ) {
				return root;
			}

			lock.unlock();

			lock = RW_LOCK.writeLock();

			lock.lock();

			root = new ConfigRootImpl( new Converters() );

			addConverters( root, (Iterable) ServiceLoader.load( ConfigConverter.class ) );
			addFactories( root, ServiceLoader.load( ConverterFactory.class ) );
			addReaders( root, ServiceLoader.load( ConfigInputReader.class ) );

			ROOTS.put( cld, root );

			return root;
		}
		finally {
			lock.unlock();
		}

	}

	private void addConverters( ConfigRootImpl root, Iterable<ConfigConverter<?>> converters )
	{
		final Converters factory = (Converters) root.converters;

		converters.forEach( factory::register );
	}

	private void addFactories( ConfigRootImpl root, Iterable<ConverterFactory> factories )
	{
		final Converters factory = (Converters) root.converters;

		factories.forEach( factory::register );
	}

	private void addReaders( ConfigRootImpl root, Iterable<ConfigInputReader> readers )
	{
		final Set<String> skip = new HashSet<>();

		readAll( root, skip, readers, "" );

		final File directory = root.getValue( ConfigInputReader.DIRECTORY_PROP, File.class ).orElse( null );
		final String[] resources = root.getValue( ConfigInputReader.RESOURCE_PROP, String[].class ).orElseGet( () -> new String[0] );

		for( final String resource : resources ) {
			if( directory != null ) {
				readAll( root, skip, readers, new File( directory, resource ).getAbsolutePath() );
			}

			readAll( root, skip, readers, resource );
		}
	}

	private void readAll( ConfigRootImpl root, Set<String> skip, Iterable<ConfigInputReader> readers, String source )
	{
		if( skip.add( source ) ) {
			for( final ConfigInputReader rd : readers ) {
				root.addConfigInputs( source.isEmpty() ? rd.read() : rd.read( source ) );
			}
		}
	}
}
