
package ascelion.config.eclipse;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

import org.eclipse.microprofile.config.spi.ConfigSource;

class ENVConfigSource implements ConfigSource
{

	private final Map<String, String> properties;

	public ENVConfigSource()
	{
		this.properties = unmodifiableMap( System.getenv() );
	}

	@Override
	public Map<String, String> getProperties()
	{
		return this.properties;
	}

	@Override
	public String getValue( String propertyName )
	{
		return this.properties.get( propertyName );
	}

	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public int getOrdinal()
	{
		return 300;
	}
}
