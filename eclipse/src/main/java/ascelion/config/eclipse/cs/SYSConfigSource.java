
package ascelion.config.eclipse.cs;

import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

import org.eclipse.microprofile.config.spi.ConfigSource;

public final class SYSConfigSource implements ConfigSource
{

	@Override
	public Map<String, String> getProperties()
	{
		return unmodifiableMap( (Map) System.getProperties() );
	}

	@Override
	public String getValue( String propertyName )
	{
		return getProperties().get( propertyName );
	}

	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public int getOrdinal()
	{
		return 400;
	}

	@Override
	public String toString()
	{
		return format( "SYS(%d)", getOrdinal() );
	}
}
