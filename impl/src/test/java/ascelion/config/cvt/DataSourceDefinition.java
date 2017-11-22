
package ascelion.config.cvt;

import java.net.URL;
import java.util.Map;

import javax.sql.DataSource;

import ascelion.config.api.ConfigValue;

public interface DataSourceDefinition
{

	Class<? extends DataSource> type();

	String jndiName();

	String serverName();

	int portName();

	String getUser();

	String getPassword();

	@ConfigValue( "values" )
	int[] intValues();

	Integer[] values();

	String url();

	URL home();

	@ConfigValue( unwrap = 3 )
	Map<String, String> getProperties();

}
