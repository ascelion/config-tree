
package ascelion.config.cdi.jmx;

import java.lang.management.ManagementFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import ascelion.cdi.junit.CdiUnit;
import ascelion.cdi.junit.EnableExtensions;
import ascelion.cdi.junit.ImportClasses;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigSource;
import ascelion.config.eclipse.ext.ConfigExt;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@ImportClasses( {
	ascelion.config.cdi.ConfigValueTest.Bean1.class,
} )
@EnableExtensions( {
	ascelion.config.cdi.ConfigExtension.class,
	ascelion.config.eclipse.cdi.ConfigExtension.class,
} )
@ConfigSource( type = JMXConfigReader.TYPE, priority = 500, value = "test" )
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
	private ConfigNode root;

	@Inject
	private ConfigExt config;

	@Test
	public void run() throws MalformedObjectNameException
	{
		final String v11 = this.config.getValue( "file.prop1", true ).get();
		final String v12 = this.root.getValue( "file.prop1" );

		assertThat( v11, equalTo( v12 ) );

		final ObjectName on = JMXTree.objectName( "test", "file.prop1" );
		final ConfigBean cb = JMX.newMBeanProxy( this.mbs, on, ConfigBean.class );

		cb.setExpression( "${java.version}" );

		final String v21 = this.config.getValue( "file.prop1", true ).get();
		final String v22 = this.root.getValue( "file.prop1" );

		assertThat( v21, equalTo( v22 ) );
		assertThat( v21, equalTo( System.getProperty( "java.version" ) ) );

		try {
			this.mbs.getObjectInstance( JMXTree.objectName( "test", "java.version" ) );

			fail( "found java.version" );
		}
		catch( final InstanceNotFoundException e ) {
			// OK
		}

		cb.setExpression( null );

		final String v31 = this.config.getValue( "file.prop1", true ).get();
		final String v32 = this.root.getValue( "file.prop1" );

		assertThat( v31, is( nullValue() ) );
		assertThat( v32, is( nullValue() ) );
	}

}
