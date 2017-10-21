
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import ascelion.shared.cdi.conf.ConfigSource.Reload;
import ascelion.tests.cdi.CdiUnit;

import static java.lang.String.format;
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
	ReloadTest.CustomSource1.class,
	ReloadTest.CustomSource2.class,
} )
public class ReloadTest
{

	static final long INTERVAL = 500;

	@Dependent
	@ConfigSource.Type( "INC1" )
	@ConfigSource( type = "INC1", reload = @Reload( value = INTERVAL / 2, unit = TimeUnit.MILLISECONDS ) )
	static class CustomSource1 implements ConfigReader
	{

		static int index;

		@Override
		public void readConfiguration( ConfigNode root, String source ) throws IOException
		{
			root.set( "value1", format( "value%01d", index++ ) );
		}
	}

	@Dependent
	@ConfigSource.Type( value = "INC2", reload = @Reload( value = 3 * INTERVAL / 2, unit = TimeUnit.MILLISECONDS ) )
	@ConfigSource( type = "INC2" )
	static class CustomSource2 implements ConfigReader
	{

		static int index;

		@Override
		public void readConfiguration( ConfigNode root, String source ) throws IOException
		{
			root.set( "value2", format( "value%01d", index++ ) );
		}
	}

	static class Bean
	{

		@ConfigValue
		String value1;

		@ConfigValue
		String value2;
	}

	@Inject
	private Instance<Bean> inst;

	@Test
	public void run() throws InterruptedException
	{
		final Bean b1 = this.inst.get();
		final Bean b2 = this.inst.get();

		Thread.sleep( INTERVAL );

		final Bean b3 = this.inst.get();
		final Bean b4 = this.inst.get();

		Thread.sleep( INTERVAL );

		final Bean b5 = this.inst.get();
		final Bean b6 = this.inst.get();

		assertThat( b1.value1, is( b2.value1 ) );
		assertThat( b1.value2, is( b2.value2 ) );

		assertThat( b1.value1, is( not( b3.value1 ) ) );
		assertThat( b1.value2, is( b4.value2 ) );

		assertThat( b1.value1, is( not( b5.value1 ) ) );
		assertThat( b1.value2, is( not( b6.value2 ) ) );
	}
}
