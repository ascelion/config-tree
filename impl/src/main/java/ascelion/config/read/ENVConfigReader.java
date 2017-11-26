
package ascelion.config.read;

import java.util.HashMap;
import java.util.Map;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigReader.Type( value = ENVConfigReader.TYPE )
@ConfigSource( type = ENVConfigReader.TYPE, priority = 300 )
public class ENVConfigReader implements ConfigReader
{

	static public final String TYPE = "ENV";

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException
	{
		final Map<String, String> map = new HashMap<>();

		System.getenv()
			.forEach( ( k, v ) -> {
				map.put( k, v.replace( ":", "\\:" ) );
			} );

		return map;
	}
}
