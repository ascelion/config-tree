
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.util.Map;

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
		public Map<String, ? extends ConfigItem> readConfiguration( String source ) throws IOException
		{
			final Gson gson = new Gson();

			return gson.fromJson( source, Map.class );
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
