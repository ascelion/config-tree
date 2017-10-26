
package ascelion.cdi.conf;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import ascelion.cdi.conf.ConfigProdBase;
import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.ConfigValue;
import ascelion.tests.cdi.CdiUnit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasses( {
	ConfigValueTest.Bean1.class,
	ConfigValueTest.Bean2.class,
	ConfigValueTest.Bean3.class,
	ConfigValueTest.BigDecimalProd.class,
} )
@UseConfigExtension
public class ConfigValueTest
{

	@ConfigSource( "file4.yml" )
	@Retention( RUNTIME )
	@Target( TYPE )
	@interface Config
	{
	}

	@ConfigSource( "file1.properties" )
	@ConfigSource( "file2.conf" )
	static class BigDecimalProd extends ConfigProdBase
	{

		@Produces
		@Dependent
		@ConfigValue( "" )
		BigDecimal create( InjectionPoint ip )
		{
			final String val = getConfigItem( ip );

			return val != null ? new BigDecimal( val ) : null;
		}
	}

	@Config
	static class Bean1
	{

		@ConfigValue( "value1:20" )
		Integer value1;

		@ConfigValue( "value2" )
		int value2;

		final String value3;

		String value4;

		String value5;

		Bean1( @ConfigValue( "value3" ) String value3 )
		{
			this.value3 = value3;
		}

		void setValues( @ConfigValue( "value4" ) String value4, @ConfigValue( "value5" ) String value5 )
		{
			this.value4 = value4;
			this.value5 = value5;
		}
	}

	@ConfigSource( "file3.ini" )
	static class Bean2
	{

		Date from;
		Date to;

		void setDate( @ConfigValue( "limits.from" ) Date from, @ConfigValue( "limits.to" ) Date to )
		{
			this.from = from;
			this.to = to;
		}
	}

	static class Bean3
	{

		final BigDecimal amount;

		Bean3( @ConfigValue( "amount:30" ) BigDecimal amount )
		{
			this.amount = amount;
		}
	}

	@Inject
	private Bean1 bean1;

	@Inject
	private Bean2 bean2;

	@Inject
	private Bean3 bean3;

	@ConfigValue( "log.file1:${sys.user.dir}/file1.log" )
	private String logFile1;

	@ConfigValue( "log.file2:${env.user.dir}/file2.log" )
	private File logFile2;

	@ConfigValue( "log.categories:2,3,4,5" )
	private List<Integer> logCategories1;

	@ConfigValue( "log.categories:2,3,4,5" )
	private Set<Integer> logCategories2;

	@ConfigValue( value = "log1.mappings" )
	private Map<String, String> map11;

	@ConfigValue( value = "log1.mappings", unwrap = 1 )
	private Map<String, Integer[]> map12;

	@ConfigValue( value = "log1.mappings", unwrap = 2 )
	private Map<String, String[]> map13;

	@ConfigValue( value = "log1.mappings", unwrap = 1 )
	private Map<String, Set<Integer>> map14;

	@ConfigValue( value = "log1.mappings", unwrap = 2 )
	private Map<String, List<String>> map15;

	@Test
	public void run()
	{
		assertThat( this.bean1, is( notNullValue() ) );
		assertThat( this.bean1.value1, is( 20 ) );
		assertThat( this.bean1.value2, is( 314 ) );
		assertThat( this.bean1.value3, is( "value3" ) );
		assertThat( this.bean1.value4, is( "value4" ) );
		assertThat( this.bean1.value5, is( "value5" ) );

		assertThat( this.bean2, is( notNullValue() ) );

		assertThat( this.bean3, is( notNullValue() ) );
		assertThat( this.bean3.amount, is( new BigDecimal( 30 ) ) );

		assertThat( "logFile1", this.logFile1, is( notNullValue() ) );
		assertThat( "logFile2", this.logFile2, is( notNullValue() ) );

		this.map11.keySet().forEach( k -> assertThat( k, k, startsWith( "log." ) ) );
		this.map12.keySet().forEach( k -> assertThat( k, k, startsWith( "log.mappings." ) ) );
		this.map13.keySet().forEach( k -> assertThat( k, k, not( startsWith( "log.mappings." ) ) ) );
	}

}
