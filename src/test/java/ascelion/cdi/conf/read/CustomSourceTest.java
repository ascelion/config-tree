
package ascelion.cdi.conf.read;

import java.util.Map;

import ascelion.cdi.conf.UseConfigExtension;
import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.ConfigValue;
import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.gson.Gson;
import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasses( {
	CustomSourceTest.CustomReader.class
} )
@UseConfigExtension
@ConfigSource( type = "custom", value = "{ 'custom.prop1': 'value', 'custom.prop2': '314' }", priority = Integer.MAX_VALUE )
public class CustomSourceTest
{

	@ConfigSource.Type( "custom" )
	static class CustomReader implements ConfigReader
	{

		@Override
		public void readConfiguration( ConfigNode root, String source )
		{
			root.setValues( new Gson().fromJson( source, Map.class ) );
		}
	}

	@ConfigValue( "custom.prop1" )
	private String sValue;

	@ConfigValue( "custom.prop2" )
	private int iValue;

	@Test
	public void run()
	{
		assertThat( this.sValue, is( "value" ) );
		assertThat( this.iValue, is( 314 ) );
	}
}
