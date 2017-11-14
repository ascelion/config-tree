
package ascelion.config.impl;

import ascelion.config.api.ConfigValue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigScannerTest
{

	@ConfigValue
	private String value;

	@Test
	public void scan()
	{
		final ConfigScanner cs = new ConfigScanner();

		System.out.println( cs.getSources() );

		assertThat( cs.getSources().size(), is( greaterThan( 0 ) ) );
	}

}
