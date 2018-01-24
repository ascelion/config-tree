
package ascelion.config.eclipse.cs;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import org.eclipse.microprofile.config.spi.ConfigSource;

public final class SYSConfigSource implements ConfigSource
{

	private final Map<String, String> properties;

	public SYSConfigSource()
	{
		this.properties = System.getProperties().stringPropertyNames().stream().collect( toMap( identity(), System::getProperty ) );
	}

	@Override
	public Map<String, String> getProperties()
	{
		return unmodifiableMap( this.properties );
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
		return 400;
	}
}
