
package ascelion.shared.cdi.conf;

import java.io.IOException;

import javax.enterprise.context.Dependent;

import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@ConfigPrefix( "cdi" )
@ConfigSource( type = "PFX" )
@AdditionalClasses( {
	PrefixTest.CustomSource.class,
} )
@UseConfigExtension
public class PrefixTest
{

	@Dependent
	@ConfigSource.Type( "PFX" )
	static class CustomSource implements ConfigReader
	{

		@Override
		public void readConfiguration( ConfigNode root, String source ) throws IOException
		{
			root.set( "cdi.prop1", "value1" );
			root.set( "cdi.prop2", "value2" );
		}
	}

	@ConfigValue
	private String prop1;

	@ConfigValue
	private String prop2;

	@Test
	public void run()
	{
		assertThat( this.prop1, is( "value1" ) );
		assertThat( this.prop2, is( "value2" ) );
	}
}
