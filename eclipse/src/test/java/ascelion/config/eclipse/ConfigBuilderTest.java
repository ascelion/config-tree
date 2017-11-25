
package ascelion.config.eclipse;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.junit.Test;

public class ConfigBuilderTest
{

	@Test
	public void run()
	{
		final ConfigBuilder cb = new ConfigBuilderImpl();

		cb.addDefaultSources();

		final Config cf = cb.build();

		final String jv = cf.getValue( "java.version", String.class );

		assertThat( jv, is( System.getProperty( "java.version" ) ) );
	}

}
