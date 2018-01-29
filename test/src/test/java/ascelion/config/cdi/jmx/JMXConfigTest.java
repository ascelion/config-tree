
package ascelion.config.cdi.jmx;

import java.lang.management.ManagementFactory;
import java.util.function.UnaryOperator;

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
import ascelion.config.utils.Expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.eclipse.microprofile.config.Config;
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
	private Config config;

	@Test
	public void run() throws MalformedObjectNameException
	{
		final String v11 = this.config.getValue( "file.prop1", String.class );
		final String v12 = this.root.getValue( "file.prop1" );

		assertEquals( v11, v12 );

		final ObjectName on = JMXTree.objectName( "test", "file.prop1" );
		final ConfigBean cb = JMX.newMBeanProxy( this.mbs, on, ConfigBean.class );

		cb.setExpression( "${java.version}" );

		final Expression exp = new Expression( (UnaryOperator<String>) x -> this.config.getValue( x, String.class ) );
		exp.setExpression( this.config.getValue( "file.prop1", String.class ) );
		final String v21 = exp.getValue();
		final String v22 = this.root.getValue( "file.prop1" );

		assertEquals( v21, v22 );
		assertEquals( System.getProperty( "java.version" ), v21 );

		try {
			this.mbs.getObjectInstance( JMXTree.objectName( "test", "java.version" ) );
			fail( "found java.version" );
		}
		catch( final InstanceNotFoundException e ) {
			// OK
		}

		this.root.getKeys().forEach( this.root::getNode );
		this.config.getPropertyNames().forEach( this.root::getNode );

		System.out.println();
	}

}
