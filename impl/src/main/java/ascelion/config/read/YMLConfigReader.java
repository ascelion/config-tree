
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ascelion.config.api.ConfigReader;

import org.yaml.snakeyaml.Yaml;

@ConfigReader.Type( value = YMLConfigReader.TYPE, types = { "YAML" } )
public class YMLConfigReader extends ResourceReader
{

	static public final String TYPE = "YML";

	@Override
	void readConfiguration( Map<String, Object> map, InputStream is ) throws IOException
	{
		final Yaml yml = new Yaml();

		yml.loadAll( is )
			.forEach( o -> {
				if( o instanceof Map ) {
					map.putAll( (Map) o );
				}
			} );
	}
}
