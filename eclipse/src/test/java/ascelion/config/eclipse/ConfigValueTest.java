
package ascelion.config.eclipse;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ascelion.cdi.junit.CdiUnit;
import ascelion.cdi.junit.EnableExtensions;
import ascelion.config.eclipse.cdi.ConfigExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@EnableExtensions( {
	ConfigExtension.class,
} )
public class ConfigValueTest
{

	static class Values
	{

		@Inject
		@ConfigProperty( name = "java.version" )
		String javaVersion;

		@Inject
		@ConfigProperty( name = "user.home" )
		String userHome;
	}

	@Inject
	Instance<Values> vi;

	@Test
	public void run()
	{
		final Values v = this.vi.get();

		assertThat( v.javaVersion, is( System.getProperty( "java.version" ) ) );
		assertThat( v.userHome, is( System.getProperty( "user.home" ) ) );
	}
}
