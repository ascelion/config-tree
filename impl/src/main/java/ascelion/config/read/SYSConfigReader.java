
package ascelion.config.read;

import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigReader.Type( value = SYSConfigReader.TYPE )
@ConfigSource( type = SYSConfigReader.TYPE, priority = 400 )
public class SYSConfigReader implements ConfigReader
{

	static public final String TYPE = "SYS";

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException
	{
		final Map<String, String> map = new TreeMap<>();

		System.getProperties()
			.forEach( ( k, v ) -> {
				map.put( (String) k, ( (String) v ).replace( ":", "\\:" ) );
			} );

		return map;
	}
}
