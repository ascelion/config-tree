
package ascelion.shared.cdi.conf;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import static java.lang.String.format;

@Dependent
class JMXSupportImpl implements JMXSupport
{

	@Inject
	private MBeanServer sv;

	@Inject
	private ConfigNode root;

	@Override
	public void register( String domain )
	{
		register( domain, this.root );
	}

	private void register( String domain, ConfigNode node )
	{
		final String item = node.getItem();
		final Map<String, ConfigNode> tree = node.getTree();

		if( item != null ) {
			try {
				final String[] keys = ConfigNode.keys( node.getPath() );
				final StringBuilder name = new StringBuilder();

				for( int k = 0; k < keys.length; k++ ) {
					if( name.length() > 0 ) {
						name.append( "," );
					}

					name.append( format( "%02d=%s", k, keys[k] ) );
				}

				name.insert( 0, domain + ":" );

				this.sv.registerMBean( new Config( node ), new ObjectName( name.toString() ) );
			}
			catch( final JMException e ) {
				e.printStackTrace();
			}
		}

		if( tree != null ) {
			tree.values().forEach( n -> register( domain, n ) );
		}
	}

}
