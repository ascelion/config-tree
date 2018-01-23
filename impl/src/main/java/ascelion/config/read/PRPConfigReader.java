
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import ascelion.config.api.ConfigReader;

@ConfigReader.Type( value = PRPConfigReader.TYPE, types = { "properties", "conf" } )
public class PRPConfigReader extends ResourceReader
{

	static public final String TYPE = "PRP";

	@Override
	void readConfiguration( Map<String, Object> map, InputStream is ) throws IOException
	{
		final Properties prp = new Properties();

		prp.load( is );
		map.putAll( (Map) prp );
	}
}
