
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public interface ConfigReader
{

	Map<String, Object> readConfiguration( URL source ) throws IOException;
}
