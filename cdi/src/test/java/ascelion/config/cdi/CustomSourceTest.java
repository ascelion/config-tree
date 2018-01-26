
package ascelion.config.cdi;

import java.util.Map;

import javax.inject.Singleton;

import ascelion.cdi.junit.CdiUnit;
import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@ConfigSource( type = "custom", value = "{ 'custom.prop1': 'value', 'custom.prop2': '314', 'custom.prop3': 'HIHI!' }", priority = 9999 )
public class CustomSourceTest
{

	@Singleton
	static class CustomConverter implements ConfigConverter<String>
	{

		@Override
		public String create( String u )
		{
			return isBlank( u ) ? null : "CUSTOM: " + u;
		}
	}

	@ConfigReader.Type( "custom" )
	static class CustomReader implements ConfigReader
	{

		@Override
		public Map<String, String> readConfiguration( String source )
		{
			return new Gson().fromJson( source, Map.class );
		}
	}

	@ConfigValue( "custom.prop1" )
	private String sValue;

	@ConfigValue( "custom.prop2" )
	private int iValue;

	@ConfigValue( value = "custom.prop3", converter = CustomConverter.class )
	private String cValue;

	@Test
	public void run()
	{
		assertThat( this.sValue, is( "value" ) );
		assertThat( this.iValue, is( 314 ) );
		assertThat( this.cValue, is( "CUSTOM: HIHI!" ) );
	}
}
