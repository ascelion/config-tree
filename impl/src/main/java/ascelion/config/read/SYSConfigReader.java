
package ascelion.config.read;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ConfigReader.Type( value = "SYS" )
@ConfigSource( type = "SYS", priority = 400 )
public class SYSConfigReader implements ConfigReader
{

	static private final Logger L = LoggerFactory.getLogger( SYSConfigReader.class );

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys ) throws ConfigException
	{
		final Map<String, String> map = new HashMap<>();

		keys.forEach( k -> {
			final String v = System.getProperty( k );

			if( v != null ) {
				L.trace( format( "overriding %s = %s", k, v ) );
				map.put( k, v );
			}
		} );

		return map;
	}
}
