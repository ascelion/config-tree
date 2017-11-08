
package ascelion.cdi.conf;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ascelion.tests.cdi.CdiUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@ConfigSource( "maps.yml" )
public class MapFailTest
{

	static class Bean
	{

		@ConfigValue( "db1" )
		Map<String, List<Set<String>>> db;
	}

	@Inject
	Instance<Bean> bean;

	@Test( expected = UnsupportedOperationException.class )
	public void run()
	{
		this.bean.get();
	}
}
