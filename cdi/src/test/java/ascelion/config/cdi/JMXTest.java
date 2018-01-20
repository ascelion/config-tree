
package ascelion.config.cdi;

import java.lang.management.ManagementFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.management.JMException;
import javax.management.MBeanServer;

import ascelion.cdi.junit.CdiUnit;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.config.read.JMXConfigReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
public class JMXTest
{

	static public void main( String[] args ) throws InterruptedException
	{
		new JUnitCore().run( JMXTest.class );

		Thread.sleep( Long.MAX_VALUE );
	}

	@ConfigSource( value = "config1", priority = 2000, type = JMXConfigReader.TYPE )
	@ConfigSource( value = "config2", priority = 2000, type = JMXConfigReader.TYPE )
	static class SP
	{

		@Produces
		private final MBeanServer serv = ManagementFactory.getPlatformMBeanServer();
	}

	@ConfigValue( "version:-${java.version}" )
	private String version;

	@Inject
	private MBeanServer mbs;

	@Test
	public void run() throws JMException
	{
		assertThat( this.mbs, is( notNullValue() ) );
		assertThat( this.version, is( System.getProperty( "java.version" ) ) );
	}
}
