
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.yaml.snakeyaml.Yaml;

@ConfigSource.Type( value = "YML", types = { "YML", "YAML" } )
@ApplicationScoped
class YMLConfigReader implements ConfigReader
{

	@Override
	public Map<String, Object> readConfiguration( URL source ) throws IOException
	{
		final Map<String, Object> map = new HashMap<>();

		try( InputStream is = source.openStream() ) {
			final Yaml yaml = new Yaml();

			yaml.loadAll( is ).forEach( o -> {
				if( o instanceof Map ) {
					map.putAll( (Map) o );
				}
			} );
		}

		return map;
	}

}
