
package ascelion.config.impl.read;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@ConfigReader.Type( value = "ENV" )
@ConfigSource( type = "ENV", priority = 300 )
public class ENVConfigReader implements ConfigReader
{

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys ) throws ConfigException
	{
		final Map<String, String> map = new HashMap<>();

		keys.forEach( k -> {
			final String v = trimToNull( System.getenv( k ) );

			if( v != null ) {
				map.put( k, v );
			}
		} );

		return map;
	}
}
