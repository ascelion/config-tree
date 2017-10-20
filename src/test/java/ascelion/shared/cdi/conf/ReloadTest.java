
package ascelion.shared.cdi.conf;

import java.io.IOException;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ascelion.shared.cdi.conf.ConfigSource.Reload;
import ascelion.tests.cdi.CdiUnit;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@AdditionalClasses( {
	ReloadTest.Bean.class,
	ReloadTest.CustomSource.class,
} )
public class ReloadTest
{

	@Dependent
	@ConfigSource.Type( "INC" )
	@ConfigSource( type = "INC", reload = @Reload( 0 ) )
	static class CustomSource implements ConfigReader
	{

		static int index;

		@Override
		public void readConfiguration( ConfigNode root, String source ) throws IOException
		{
			root.set( "value", format( "value%01d", index++ ) );
		}
	}

	static class Bean
	{

		@ConfigValue( "value" )
		String value;
	}

	@Inject
	private Instance<Bean> inst;

	@Test
	public void run()
	{
		final Bean b1 = this.inst.get();
		final Bean b2 = this.inst.get();

		assertThat( b1, is( not( sameInstance( b2 ) ) ) );

		assertThat( b1.value, is( not( b2.value ) ) );
	}
}
