
package ascelion.cdi.conf;

import java.util.Map;

import javax.inject.Inject;

import ascelion.shared.cdi.conf.ConfigPrefix;
import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.ConfigValue;
import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@ConfigSource( "maps.yml" )
public class MapTest
{

	static class Bean1
	{

		@ConfigValue( value = "db" )
		Map<String, String> db1;

		@ConfigValue( value = "db.eclipselink", unwrap = 1 )
		Map<String, String> db2;
	}

	@ConfigPrefix( "db" )
	static class Bean2
	{

		@ConfigValue( value = "eclipselink", unwrap = 1 )
		Map<String, String> db;
	}

	@Inject
	Bean1 b1;

	@Inject
	Bean2 b2;

	@Test
	public void run()
	{
		assertThat( this.b1, is( notNullValue() ) );
		assertThat( this.b1.db1, is( notNullValue() ) );
		assertThat( this.b1.db2, is( notNullValue() ) );

		assertThat( this.b1.db1.size(), greaterThan( 0 ) );
		assertThat( this.b1.db2.size(), greaterThan( 0 ) );

		this.b1.db1.entrySet().forEach( e -> {
			assertThat( e.getKey(), e.getKey(), startsWith( "db." ) );
			assertThat( e.getKey(), e.getValue(), is( notNullValue() ) );
		} );
		this.b1.db2.entrySet().forEach( e -> {
			assertThat( e.getKey(), e.getKey(), startsWith( "eclipselink." ) );
			assertThat( e.getKey(), e.getValue(), is( notNullValue() ) );
		} );

		assertThat( this.b2, is( notNullValue() ) );
		assertThat( this.b2.db, is( notNullValue() ) );
		this.b2.db.entrySet().forEach( e -> {
			assertThat( e.getKey(), e.getKey(), startsWith( "eclipselink." ) );
			assertThat( e.getKey(), e.getValue(), is( notNullValue() ) );
		} );
	}
}
