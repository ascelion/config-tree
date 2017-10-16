
package ascelion.shared.cdi.conf;

import java.util.Map;

public interface ConfigProvider
{

	Map<String, Object> getConfiguration();
}
