
package ascelion.shared.cdi.conf;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasses( {
	SimpleTest.StringProd.class,
} )
@UseConfigExtension
public class SimpleTest
{

	@ApplicationScoped
	static class StringProd
	{

		@Produces
		@ConfigValue( "" )
		String get( InjectionPoint ip )
		{
			return System.getProperty( ip.getAnnotated().getAnnotation( ConfigValue.class ).value() );
		}
	}

	@ConfigValue( "java.version" )
	private String version;

	@ConfigValue( "java.io.tmpdir" )
	private File temp;

	@Test
	public void run()
	{
		assertThat( this.version, is( System.getProperty( "java.version" ) ) );
	}
}
