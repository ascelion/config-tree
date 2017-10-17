
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.yaml.snakeyaml.Yaml;

@ConfigSource.Type( value = "YML", types = { "YML", "YAML" } )
@ApplicationScoped
class YMLConfigReader extends ConfigStore implements ConfigReader
{

	@Override
	public Map<String, Object> readConfiguration( InputStream is ) throws IOException
	{
		final Yaml yaml = new Yaml();

		yaml.loadAll( is ).forEach( o -> {
			if( o instanceof Map ) {
				add( (Map<String, Object>) o );
			}
		} );

		return get();
	}

}
