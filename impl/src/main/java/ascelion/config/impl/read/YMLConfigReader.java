
package ascelion.config.impl.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import org.yaml.snakeyaml.Yaml;

@ConfigReader.Type( value = "YML", types = { "YML", "YAML" } )
public class YMLConfigReader implements ConfigReader
{

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys, InputStream is ) throws IOException
	{
		final Map<String, ?> map = new HashMap<>();
		final Yaml yml = new Yaml();

		yml.loadAll( is )
			.forEach( o -> {
				if( o instanceof Map ) {
					map.putAll( (Map) o );
				}
			} );

		return map;
	}
}
