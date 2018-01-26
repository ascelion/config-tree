
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;

public abstract class ResourceReader implements ConfigReader
{

	private final Map<String, Long> times = new ConcurrentHashMap<>();

	@Override
	public boolean isModified( String source )
	{
		long time = 0;

		try {
			final URL u = new URL( source );
			final URLConnection c = u.openConnection();

			time += c.getLastModified();
		}
		catch( final IOException e ) {
			throw new ConfigException( source, e );
		}

		final Long old = this.times.put( source, time );

		return old == null || Long.compareUnsigned( old, time ) < 0;
	}

	@Override
	public Map<String, String> readConfiguration( String source ) throws ConfigException
	{
		long time = 0;

		try {
			final URL u = new URL( source );
			final URLConnection sc = u.openConnection();

			time += sc.getLastModified();

			try( InputStream is = sc.getInputStream() ) {
				return readConfiguration( is );
			}
			finally {
				this.times.put( source, time );
			}
		}
		catch( final IOException e ) {
			throw new ConfigException( source, e );
		}
	}

	protected abstract Map<String, String> readConfiguration( InputStream is ) throws IOException;
}
