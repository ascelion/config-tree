
package ascelion.config.impl;

import java.lang.management.ManagementFactory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.management.JMException;
import javax.management.MBeanServer;

import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
public class JMXTest
{

	@ConfigSource( value = "config", priority = 2000, type = "JMX" )
	static class SP
	{

		@Produces
		private final MBeanServer serv = ManagementFactory.getPlatformMBeanServer();
	}

	@ConfigValue( "version:${java.version}" )
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
