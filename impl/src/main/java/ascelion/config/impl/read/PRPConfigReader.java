
package ascelion.config.impl.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.context.Dependent;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigSource.Type( value = "properties", types = "conf" )
@Dependent
public class PRPConfigReader implements ConfigReader
{

	@Override
	public void readConfiguration( ConfigSource source, ConfigNode root, InputStream is ) throws IOException
	{
		final Properties prop = new Properties();

		prop.load( is );
		prop.forEach( ( k, v ) -> root.setValue( (String) k, (String) v ) );
	}
}
