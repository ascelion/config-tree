
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigReader.Type( value = PRPConfigReader.TYPE, types = { "properties", "conf" } )
public class PRPConfigReader implements ConfigReader
{

	static public final String TYPE = "PRP";

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source, InputStream is ) throws IOException
	{
		final Map<String, String> map = new HashMap<>();
		final Properties prp = new Properties();

		prp.load( is );
		map.putAll( (Map) prp );

		return map;
	}
}
