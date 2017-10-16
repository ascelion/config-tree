
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

@ConfigSource.Type( value = "properties", types = "conf" )
@ApplicationScoped
class PRPConfigReader implements ConfigReader
{

	@Override
	public Map<String, Object> readConfiguration( URL source ) throws IOException
	{
		final ConfigMap cm = new ConfigMap();

		try( InputStream is = source.openStream() ) {
			final Properties prop = new Properties();

			prop.load( is );

			prop.forEach( ( k, v ) -> cm.setValue( (String) k, (String) v ) );
		}

		return cm.get();
	}

}
