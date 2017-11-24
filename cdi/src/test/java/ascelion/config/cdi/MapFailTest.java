
package ascelion.config.cdi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.tests.cdi.CdiUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@ConfigSource( "maps.yml" )
@Ignore( "we are going to support this" )
public class MapFailTest
{

	static class Bean
	{

		@ConfigValue( "db1" )
		Map<String, List<Set<String>>> db;
	}

	@Inject
	Instance<Bean> bean;

	@Test( expected = ConfigException.class )
	public void run()
	{
		this.bean.get();
	}
}
