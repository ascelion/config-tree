
package ascelion.config.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigReader.Type( "XYZ" )
@ConfigSource( type = "XYZ", value = "xyz.prop: value1" )
class XYZReader implements ConfigReader
{

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException
	{
		final Map<String, String> m = new HashMap<>();
		final Properties p = new Properties();

		try {
			p.load( new StringReader( source.value() ) );
		}
		catch( final IOException e ) {
			e.printStackTrace();
		}

		m.putAll( (Map) p );

		return m;
	}
}
