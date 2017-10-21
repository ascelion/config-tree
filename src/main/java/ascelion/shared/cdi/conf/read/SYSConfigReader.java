
package ascelion.shared.cdi.conf.read;

import java.io.IOException;

import javax.enterprise.context.Dependent;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

@ConfigSource( priority = 1000, type = "SYS" )
@ConfigSource.Type( value = "SYS" )
@Dependent
public class SYSConfigReader implements ConfigReader
{

	@Override
	public void readConfiguration( ConfigNode root, String source ) throws IOException
	{
		root.asMap( x -> x ).keySet().forEach( k -> {
			final String v = System.getProperty( k );

			if( v != null ) {
				root.set( k, v );
			}
		} );
	}
}
