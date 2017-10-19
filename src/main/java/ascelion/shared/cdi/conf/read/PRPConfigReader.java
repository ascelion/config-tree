
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import ascelion.shared.cdi.conf.ConfigItem;
import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.ConfigStore;

@ConfigSource.Type( value = "properties", types = "conf" )
@ApplicationScoped
class PRPConfigReader extends ConfigStore implements ConfigReader
{

	@Override
	public Map<String, ? extends ConfigItem> readConfiguration( InputStream is ) throws IOException
	{
		reset();

		final Properties prop = new Properties();

		prop.load( is );
		prop.forEach( ( k, v ) -> setValue( (String) k, (String) v ) );

		return get();
	}

	@Override
	public void readConfiguration( ConfigNode root, InputStream is ) throws IOException
	{
		final Properties prop = new Properties();

		prop.load( is );
		prop.forEach( ( k, v ) -> root.set( (String) k, v ) );
	}
}
