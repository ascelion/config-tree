
package ascelion.config.impl.read;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigReader.Type( value = "SYS" )
@ConfigSource( type = "SYS", priority = 400 )
public class SYSConfigReader implements ConfigReader
{

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys ) throws ConfigException
	{
		final Map<String, String> map = new HashMap<>();

		keys.forEach( k -> {
			final String v = System.getProperty( k );

			if( v != null ) {
				map.put( k, v );
			}
		} );

		return map;
	}
}
