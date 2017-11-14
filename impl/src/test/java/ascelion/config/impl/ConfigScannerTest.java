
package ascelion.config.impl;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigValue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigScannerTest
{

	@ConfigReader.Type( "XXX" )
	static class Reader implements ConfigReader
	{
	}

	static class Bean
	{

		int intValue;
		float floatValue;

		@ConfigValue
		void setIntValue( int intValue )
		{
			this.intValue = intValue;
		}

		void setFloatValue( @ConfigValue( "floatValue" ) float floatValue )
		{
			this.floatValue = floatValue;
		}
	}

	@ConfigValue
	private String value;

	@Test
	public void scan()
	{
		final ConfigScanner cs = new ConfigScanner();

		System.out.println( cs.getSources() );
		System.out.println( cs.getValues() );
		System.out.println( cs.getReaders() );

		assertThat( cs.getSources().size(), is( greaterThan( 0 ) ) );
		assertThat( cs.getValues().size(), is( greaterThan( 0 ) ) );
		assertThat( cs.getReaders().size(), is( greaterThan( 0 ) ) );
	}

}
