
package ascelion.config.cdi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.Dependent;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigPrefix;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
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
	@ConfigReader.Type( "PFX" )
	static class CustomSource implements ConfigReader
	{

		@Override
		public Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys ) throws ConfigException
		{
			final Map<String, String> map = new HashMap<>();

			map.put( "cdi.prop1", "value1" );
			map.put( "cdi.prop2", "value2" );

			return map;
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
