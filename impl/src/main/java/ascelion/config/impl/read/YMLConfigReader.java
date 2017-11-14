
package ascelion.config.impl.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import org.yaml.snakeyaml.Yaml;

@ConfigReader.Type( value = "YML", types = { "YML", "YAML" } )
public class YMLConfigReader implements ConfigReader
{

	@Override
	public void readConfiguration( ConfigSource source, ConfigNode root, InputStream is ) throws IOException
	{
		final Yaml yaml = new Yaml();

		yaml.loadAll( is )
			.forEach( o -> {
				if( o instanceof Map ) {
					root.setValues( null, (Map<String, ?>) o );
				}
			} );
	}
}
