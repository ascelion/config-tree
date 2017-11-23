
package ascelion.config.cdi;

import java.io.File;

import ascelion.config.api.ConfigValue;
import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
public class SimpleTest
{

	@ConfigValue( "java.version" )
	private String version;

	@ConfigValue( "java.io.tmpdir" )
	private File temp;

	@Test
	public void run()
	{
		assertThat( this.version, is( System.getProperty( "java.version" ) ) );
		assertThat( this.temp, is( new File( System.getProperty( "java.io.tmpdir" ) ) ) );
	}
}
