
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static java.util.Collections.emptyMap;

public interface ConfigReader
{

	default Map<String, ?> readConfiguration( String source ) throws IOException
	{
		return emptyMap();
	}

	default Map<String, ?> readConfiguration( URL source ) throws IOException
	{
		try( InputStream is = source.openStream() ) {
			return readConfiguration( is );
		}
	}

	default Map<String, ?> readConfiguration( InputStream source ) throws IOException
	{
		throw new UnsupportedOperationException( "" );
	}
}
