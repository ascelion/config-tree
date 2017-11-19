
package ascelion.config.impl.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigReader.Type( value = "properties", types = "conf" )
public class PRPConfigReader implements ConfigReader
{

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys, InputStream is ) throws IOException
	{
		final Map<String, String> map = new HashMap<>();
		final Properties prp = new Properties();

		prp.load( is );
		map.putAll( (Map) prp );

		return map;
	}
}
