
package ascelion.config.read;

import java.util.Map;

import ascelion.config.api.ConfigReader;
import ascelion.logging.LOG;

import org.eclipse.microprofile.config.spi.ConfigSource;

final class AnnotatedConfigSource implements ConfigSource
{

	static private final LOG L = LOG.get();

	private final ConfigReader rd;
	private Map<String, String> properties;
	private final String source;
	private final int priority;

	AnnotatedConfigSource( ConfigReader rd, String source, int priority )
	{
		this.rd = rd;
		this.source = source;
		this.priority = priority;
	}

	@Override
	public synchronized Map<String, String> getProperties()
	{
		if( this.properties == null || this.rd.isModified( this.source ) ) {
			L.trace( "Reading '%s'", this.source );

			this.properties = this.rd.readConfiguration( this.source );
		}

		return this.properties;
	}

	@Override
	public String getValue( String propertyName )
	{
		return getProperties().get( propertyName );
	}

	@Override
	public String getName()
	{
		return this.source;
	}

	@Override
	public int getOrdinal()
	{
		return this.priority;
	}
}
