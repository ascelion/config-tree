
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

import org.yaml.snakeyaml.Yaml;

@ConfigSource.Type( value = "YML", types = { "YML", "YAML" } )
@ApplicationScoped
class YMLConfigReader implements ConfigReader
{

	@Override
	public void readConfiguration( ConfigNode root, InputStream source ) throws IOException
	{
		final Yaml yaml = new Yaml();

		yaml.loadAll( source )
			.forEach( o -> {
				if( o instanceof Map ) {
					root.set( o );
				}
			} );
	}
}
