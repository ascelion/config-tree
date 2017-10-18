
package ascelion.shared.cdi.conf;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import ascelion.tests.cdi.AdditionalAnnotations;
import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jglue.cdiunit.AdditionalClasspaths;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasspaths( {
	ConfigExtension.class,
//	SimpleTest.StringProd.class,
} )
@AdditionalAnnotations(
	annotations = {
		ApplicationScoped.class,
	},
	fromPackages = {
		ConfigExtension.class,
	} )
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

	@Test
	public void run()
	{
		assertThat( this.version, is( System.getProperty( "java.version" ) ) );
	}
}
