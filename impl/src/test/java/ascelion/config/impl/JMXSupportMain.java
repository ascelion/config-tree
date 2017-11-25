
package ascelion.config.impl;

import java.lang.management.ManagementFactory;

public class JMXSupportMain
{

	static public void main( String[] args ) throws InterruptedException
	{
		final ConfigJava cj = new ConfigJava();
		final JMXSupport jmx = new JMXSupport( ManagementFactory.getPlatformMBeanServer(), "config" );

		jmx.buildEntries( cj.root() );

		Thread.sleep( Long.MAX_VALUE );
	}
}
