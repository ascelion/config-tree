
package ascelion.config.eclipse.cs;

import java.util.Map;

import ascelion.config.eclipse.ext.ConfigSourceExt;

import static java.util.Collections.unmodifiableMap;

public final class ENVConfigSource implements ConfigSourceExt
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
