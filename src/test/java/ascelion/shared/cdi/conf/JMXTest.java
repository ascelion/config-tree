
package ascelion.shared.cdi.conf;

import java.lang.management.ManagementFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.management.JMException;
import javax.management.MBeanServer;

import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.JMXSupport;
import ascelion.tests.cdi.CdiUnit;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@UseConfigExtension
@AdditionalClasses( {
	JMXTest.SP.class,
	GenericTest.CustomSource.class,
	ReloadTest.CustomSource1.class,
	ReloadTest.CustomSource2.class,
} )
@ConfigSource( "config.ini" )
@ConfigSource( "config.properties" )
@ConfigSource( "config.xml" )
@ConfigSource( "config.yml" )
@ConfigSource( "file1.properties" )
@ConfigSource( "file2.conf" )
@ConfigSource( "file3.ini" )
@ConfigSource( "file4.yml" )
@ConfigSource( "maps.yml" )
public class JMXTest
{

	static class SP
	{

		@Produces
		private final MBeanServer serv = ManagementFactory.getPlatformMBeanServer();
	}

	@Inject
	private JMXSupport jmx;

	@Test
	public void run() throws JMException
	{
		this.jmx.register( "config" );
	}
}
