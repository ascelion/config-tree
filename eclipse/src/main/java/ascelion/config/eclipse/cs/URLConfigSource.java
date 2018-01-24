
package ascelion.config.eclipse.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ascelion.logging.LOG;

import static java.lang.String.format;

import org.eclipse.microprofile.config.spi.ConfigSource;

public abstract class URLConfigSource implements ConfigSource
{

	static private final LOG L = LOG.get();

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Map<String, String> properties = new TreeMap<>();
	private final URL resource;
	private long updated = -1;

	public URLConfigSource( URL resource )
	{
		this.resource = resource;
	}

	@Override
	public final Map<String, String> getProperties()
	{
		this.rwl.readLock().lock();

		try {
			load();

			return this.properties;
		}
		finally {
			this.rwl.readLock().unlock();
		}
	}

	@Override
	public final String getValue( String propertyName )
	{
		this.rwl.readLock().lock();

		try {
			load();

			return this.properties.get( propertyName );
		}
		finally {
			this.rwl.readLock().unlock();
		}
	}

	@Override
	public final String getName()
	{
		return this.resource.toExternalForm();
	}

	private void load()
	{
		try {
			final long lastModified = this.resource.openConnection().getLastModified();

			if( lastModified > this.updated ) {
				this.rwl.readLock().unlock();

				try {
					this.rwl.writeLock().lock();

					try {
						try( InputStream is = this.resource.openStream() ) {
							final Map<String, String> map = new TreeMap<>();

							readConfiguration( map, is );

							this.properties.clear();
							this.properties.putAll( map );

							this.updated = lastModified;

							if( L.isTraceEnabled() ) {
								final StringWriter w = new StringWriter();

								map.forEach( ( k, v ) -> w.append( format( "%s = %s\n", k, v ) ) );

								L.trace( "LastUpdated %tF, %s\n%s", this.updated, getName(), w );
							}
						}
					}
					finally {
						this.rwl.writeLock().unlock();
					}
				}
				finally {
					this.rwl.readLock().lock();
				}
			}
		}
		catch( final IOException e ) {
			e.printStackTrace();
		}
	}

	abstract void readConfiguration( Map<String, String> map, InputStream is ) throws IOException;
}
