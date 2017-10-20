
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.context.Dependent;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

@ConfigSource.Type( value = "properties", types = "conf" )
@Dependent
class PRPConfigReader implements ConfigReader
{

	@Override
	public void readConfiguration( ConfigNode root, InputStream is ) throws IOException
	{
		final Properties prop = new Properties();

		prop.load( is );
		prop.forEach( ( k, v ) -> root.set( (String) k, v ) );
	}
}
