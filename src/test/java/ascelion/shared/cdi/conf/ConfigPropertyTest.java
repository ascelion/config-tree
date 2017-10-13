
package ascelion.shared.cdi.conf;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import ascelion.tests.cdi.CdiUnit;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.impl.config.ConfigurationExtension;
import org.apache.deltaspike.core.spi.config.BaseConfigPropertyProducer;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasses( {
	ConfigPropertyTest.Bean1.class,
	ConfigPropertyTest.Bean2.class,
	ConfigPropertyTest.Bean3.class,
	ConfigPropertyTest.BigDecimalCVT.class,
	ConfigPropertyTest.DateCVT.class,
	ConfigPropertyTest.FileCVT.class,
} )
@AdditionalClasspaths( {
	BeanManagerProvider.class,
	ConfigurationExtension.class,
} )
public class ConfigPropertyTest
{

	@ApplicationScoped
	static class FileCVT implements ConfigResolver.Converter<File>
	{

		@Override
		public File convert( String value )
		{
			return new File( value );
		}
	}

	@ApplicationScoped
	static class BigDecimalCVT extends BaseConfigPropertyProducer
	{

		@Produces
		@Dependent
		@ConfigProperty( name = "" )
		BigDecimal create( InjectionPoint ip )
		{
			final String val = getStringPropertyValue( ip );

			return val != null ? new BigDecimal( val ) : null;
		}
	}

	@ApplicationScoped
	static class DateCVT implements ConfigResolver.Converter<Date>
	{

		@Override
		public Date convert( String value )
		{
			try {
				return new SimpleDateFormat().parse( value );
			}
			catch( final ParseException e ) {
				throw new RuntimeException( e );
			}
		}
	}

	static class Bean1
	{

		@Inject
		@ConfigProperty( name = "value1:20" )
		Integer value1;

		@Inject
		@ConfigProperty( name = "value2" )
		private int value2;

		final String value3;

		String value4;

		String value5;

		@Inject
		Bean1( @ConfigProperty( name = "value3" ) String value3 )
		{
			this.value3 = value3;
		}

		@Inject
		void setValues( @ConfigProperty( name = "value4" ) String value4, @ConfigProperty( name = "value5" ) String value5 )
		{
			this.value4 = value4;
			this.value5 = value5;
		}
	}

	static class Bean2
	{

		@Inject
		public void setDate( @ConfigProperty( name = "limits.from", converter = DateCVT.class ) Date from,
				@ConfigProperty( name = "limits.to", converter = DateCVT.class ) Date to )
		{
		}
	}

	static class Bean3
	{

		@Inject
		public Bean3( @ConfigProperty( name = "amount" ) BigDecimal amount )
		{
		}
	}

	@Inject
	@ConfigProperty( name = "log.file1", defaultValue = "${user.dir}/file1.log" )
	private String logFile1;

	@Inject
	@ConfigProperty( name = "log.file2:${env.user.dir}/file2.log", converter = FileCVT.class )
	private File logFile2;

	@Inject
	Bean1 bean1;

	@Test
	public void run()
	{
//		assertNotNull( this.bean1 );
//		assertNotNull( "logFile1", this.logFile1 );
//		assertNotNull( "logFile2", this.logFile2 );
	}
}
