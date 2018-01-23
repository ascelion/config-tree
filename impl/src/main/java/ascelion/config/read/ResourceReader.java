
package ascelion.config.read;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static java.util.Collections.list;
import static java.util.Collections.singletonList;

public abstract class ResourceReader implements ConfigReader
{

	static List<URL> getResources( String source )
	{
		try {
			return singletonList( new URL( source ) );
		}
		catch( final MalformedURLException e ) {
			;
		}

		final List<URL> keys = new ArrayList<>();

		try {
			keys.addAll( list( Thread.currentThread().getContextClassLoader().getResources( source ) ) );

			final File file = new File( source );

			if( file.exists() ) {
				keys.add( file.toURI().toURL() );
			}

			return keys;
		}
		catch( final IOException e ) {
			throw new ConfigException( source, e );
		}
	}

	private final Map<ConfigSource, Long> times = new ConcurrentHashMap<>();

	@Override
	public boolean isModified( ConfigSource source )
	{
		long time = 0;

		for( final URL u : getResources( source.value() ) ) {
			try {
				final URLConnection sc = u.openConnection();

				time += sc.getLastModified();
			}
			catch( final IOException e ) {
				throw new ConfigException( u.toExternalForm(), e );
			}
		}

		final Long old = this.times.put( source, time );

		return old == null || Long.compareUnsigned( old, time ) < 0;
	}

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException
	{
		final Map<String, Object> map = new TreeMap<>();

		long time = 0;

		for( final URL u : getResources( source.value() ) ) {
			try {
				final URLConnection sc = u.openConnection();

				time += sc.getLastModified();

				try( InputStream is = sc.getInputStream() ) {
					readConfiguration( map, is );
				}
			}
			catch( final IOException e ) {
				throw new ConfigException( u.toExternalForm(), e );
			}
		}

		this.times.put( source, time );

		return map;
	}

	abstract void readConfiguration( Map<String, Object> map, InputStream is ) throws IOException;
}
