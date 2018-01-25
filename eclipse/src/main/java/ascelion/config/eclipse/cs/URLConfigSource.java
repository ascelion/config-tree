
package ascelion.config.eclipse.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ascelion.logging.LOG;

import static java.lang.String.format;

import org.eclipse.microprofile.config.spi.ConfigSource;

public abstract class URLConfigSource implements ConfigSource
{

	static private final LOG L = LOG.get();

	private final ReadWriteLock rwl = new ReentrantReadWriteLock();
	private Map<String, String> properties;
	private final URL resource;
	private long updated = 0;

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
			long lastModified = this.resource.openConnection().getLastModified();

			if( this.properties == null || lastModified > this.updated ) {
				this.rwl.readLock().unlock();

				try {
					this.rwl.writeLock().lock();

					lastModified = this.resource.openConnection().getLastModified();

					if( this.properties != null && lastModified <= this.updated ) {
						return;
					}

					try {
						try( InputStream is = this.resource.openStream() ) {
							this.properties = readConfiguration( is );
							this.updated = lastModified;

							if( L.isTraceEnabled() ) {
								final StringWriter w = new StringWriter();

								this.properties.forEach( ( k, v ) -> w.append( format( "%s = %s\n", k, v ) ) );

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
			L.warn( "Cannot read from %s", getName() );
		}
	}

	abstract Map<String, String> readConfiguration( InputStream is ) throws IOException;
}
