
package ascelion.config.cdi.jmx;

import java.lang.management.ManagementFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import ascelion.cdi.junit.CdiUnit;
import ascelion.cdi.junit.EnableExtensions;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigSource;
import ascelion.config.cdi.ConfigExtension;

import static org.junit.Assert.assertEquals;

import org.eclipse.microprofile.config.Config;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@EnableExtensions( {
	ConfigExtension.class,
	ascelion.config.eclipse.cdi.ConfigExtension.class,
} )
@ConfigSource( type = "JMX", priority = 500, value = "test" )
@ConfigSource( "file.properties" )
public class JMXConfigTest
{

	static class JMXFactory
	{

		@Produces
		@Singleton
		private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	}

	@Inject
	private MBeanServer mbs;

	@Inject
	private Instance<ConfigNode> root;

	@Inject
	private Config config;

	@Test
	public void run() throws MalformedObjectNameException
	{
		final String v11 = this.config.getValue( "file.prop1", String.class );
		final String v12 = this.root.get().getValue( "file.prop1" );

		assertEquals( v11, v12 );

		final ObjectName on = new ObjectName( "test:00=file,01=prop1" );
		final ConfigBean cb = JMX.newMBeanProxy( this.mbs, on, ConfigBean.class );

		cb.setValue( cb.getValue() + "CHANGED" );

		final String v21 = this.config.getValue( "file.prop1", String.class );
		final String v22 = this.root.get().getValue( "file.prop1" );

		assertEquals( v21, v22 );
		assertEquals( v11 + "CHANGED", v21 );
	}

}
