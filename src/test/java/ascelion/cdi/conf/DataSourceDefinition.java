
package ascelion.cdi.conf;

import java.net.URL;
import java.util.Map;

import javax.sql.DataSource;

public interface DataSourceDefinition
{

	Class<? extends DataSource> type();

	String jndiName();

	String serverName();

	int portName();

	String getUser();

	String getPassword();

	// TODO
	//int[] values();
	Integer[] values();

	String url();

	URL home();

	@ConfigValue( unwrap = 3 )
	Map<String, String> getProperties();

}
