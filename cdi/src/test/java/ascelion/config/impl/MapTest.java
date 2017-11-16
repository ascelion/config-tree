
package ascelion.config.impl;

import java.util.Map;

import javax.inject.Inject;

import ascelion.config.api.ConfigPrefix;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@ConfigSource( "maps.yml" )
@Ignore
public class MapTest
{

	static class Bean1
	{

		@ConfigValue( value = "db1" )
		Map<String, String> db1;

		@ConfigValue( value = "db1.eclipselink", unwrap = 1 )
		Map<String, String> db2;
	}

	@ConfigPrefix( "db1" )
	static class Bean2
	{

		@ConfigValue( value = "eclipselink", unwrap = 1 )
		Map<String, String> db;
	}

	@ConfigPrefix( "db2" )
	static class Bean3
	{

		@ConfigValue( value = "javax", unwrap = 1 )
		Map<String, String> db;
	}

	@Inject
	Bean1 b1;

	@Inject
	Bean2 b2;

	@Inject
	Bean3 b3;

	@Test
	public void run()
	{
		assertThat( this.b1, is( notNullValue() ) );
		assertThat( this.b1.db1, is( notNullValue() ) );
		assertThat( this.b1.db2, is( notNullValue() ) );

		assertThat( this.b1.db1.size(), greaterThan( 0 ) );
		assertThat( this.b1.db2.size(), greaterThan( 0 ) );

		this.b1.db1.entrySet().forEach( e -> {
			assertThat( e.getKey(), e.getKey(), startsWith( "db1." ) );
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

		assertThat( this.b3, is( notNullValue() ) );
		assertThat( this.b3.db, is( notNullValue() ) );
		this.b3.db.entrySet().forEach( e -> {
			assertThat( e.getKey(), e.getKey(), startsWith( "javax." ) );
			assertThat( e.getKey(), e.getValue(), is( notNullValue() ) );
		} );
	}
}
