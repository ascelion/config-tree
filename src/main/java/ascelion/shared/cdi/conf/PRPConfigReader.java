
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

@ConfigSource.Type( value = "properties", types = "conf" )
@ApplicationScoped
class PRPConfigReader extends ConfigStore implements ConfigReader
{

	@Override
	public Map<String, Object> readConfiguration( InputStream is ) throws IOException
	{
		final Properties prop = new Properties();

		prop.load( is );
		prop.forEach( ( k, v ) -> setValue( (String) k, (String) v ) );

		return get();
	}

}
