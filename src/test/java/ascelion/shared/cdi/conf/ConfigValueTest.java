
package ascelion.shared.cdi.conf;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import ascelion.tests.cdi.CdiUnit;

import static org.junit.Assert.assertNotNull;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasses( {
	ConfigExtension.class,
	ConfigValueTest.Bean1.class,
	ConfigValueTest.Bean2.class,
	ConfigValueTest.Bean3.class,
	ConfigValueTest.BigDecimalProd.class,
} )
@AdditionalClasspaths( {
	BeanManagerProvider.class,
} )
public class ConfigValueTest
{

	static class BigDecimalProd extends ConfigProdBase
	{

		@Produces
		@Dependent
		@ConfigValue( "" )
		BigDecimal create( InjectionPoint ip )
		{
			final String val = getProperty( ip );

			return val != null ? new BigDecimal( val ) : null;
		}
	}

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

//	@ConfigValue( value = "log.mappings", unwrap = "log" )
//	private Map<String, Object> logMappings;

	@Test
	public void run()
	{
		assertNotNull( this.bean1 );
		assertEqual( 20, this.bean1.value1 );
		assertEqual( 0, this.bean1.value2 );

		assertNotNull( this.bean2 );
		assertNotNull( this.bean3 );

		assertNotNull( this.bean3.amount );

		assertNotNull( "logFile1", this.logFile1 );
		assertNotNull( "logFile2", this.logFile2 );
	}

	private void assertEqual( int i, Integer value1 )
	{
	}
}
