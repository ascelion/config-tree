
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import static java.lang.String.format;

import org.ini4j.Ini;

@ConfigSource.Type( value = "INI" )
@ApplicationScoped
class INIConfigReader implements ConfigReader
{

	@Override
	public Map<String, Object> readConfiguration( URL source ) throws IOException
	{
		final ConfigMap map = new ConfigMap();

		try( InputStream is = source.openStream() ) {
			final Ini ini = new Ini( is );

			ini.forEach( ( k0, v0 ) -> {
				v0.forEach( ( k1, v1 ) -> {
					map.setValue( format( "%s.%s", k0, k1 ), v1 );
				} );
			} );
		}

		return map.get();
	}

}
