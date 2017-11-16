
package ascelion.config.impl.read;

import java.util.Map;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.config.impl.UseConfigExtension;
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

	@ConfigReader.Type( "custom" )
	static class CustomReader implements ConfigReader
	{

		@Override
		public void readConfiguration( ConfigSource source, ConfigNode root )
		{
			root.setValues( new Gson().fromJson( source.value(), Map.class ) );
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