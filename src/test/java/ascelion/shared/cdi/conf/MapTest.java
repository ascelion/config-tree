
package ascelion.shared.cdi.conf;

import java.util.Map;

import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@ConfigSource( "maps.yml" )
public class MapTest
{

	@ConfigValue( value = "db" )
	Map<String, String> db1;

	@ConfigValue( value = "db.eclipselink", unwrap = 1 )
	Map<String, String> db2;

	@Test
	public void run()
	{
		assertThat( this.db1, is( notNullValue() ) );
		assertThat( this.db2, is( notNullValue() ) );

		this.db1.keySet().forEach( k -> assertThat( k, k, startsWith( "db." ) ) );
		this.db2.keySet().forEach( k -> assertThat( k, k, startsWith( "eclipselink." ) ) );
	}
}
