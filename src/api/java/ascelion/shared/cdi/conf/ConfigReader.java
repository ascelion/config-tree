
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public interface ConfigReader
{

	default Map<String, ? extends ConfigItem> readConfiguration( URL source ) throws IOException
	{
		try( InputStream is = source.openStream() ) {
			return readConfiguration( is );
		}
	}

	Map<String, ? extends ConfigItem> readConfiguration( InputStream source ) throws IOException;
}
