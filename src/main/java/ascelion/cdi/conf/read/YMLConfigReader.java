
package ascelion.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.Dependent;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

import org.yaml.snakeyaml.Yaml;

@ConfigSource.Type( value = "YML", types = { "YML", "YAML" } )
@Dependent
class YMLConfigReader implements ConfigReader
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
