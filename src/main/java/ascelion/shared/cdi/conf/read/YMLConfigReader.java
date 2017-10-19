
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import ascelion.shared.cdi.conf.ConfigItem;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.ConfigStore;

import org.yaml.snakeyaml.Yaml;

@ConfigSource.Type( value = "YML", types = { "YML", "YAML" } )
@ApplicationScoped
class YMLConfigReader extends ConfigStore implements ConfigReader
{

	@Override
	public Map<String, ? extends ConfigItem> readConfiguration( InputStream is ) throws IOException
	{
		reset();

		final Yaml yaml = new Yaml();

		yaml.loadAll( is ).forEach( o -> {
			if( o instanceof Map ) {
				add( (Map<String, Object>) o );
			}
		} );

		return get();
	}

}
