
package ascelion.shared.cdi.conf;

import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
class JMXSupportImpl implements JMXSupport
{

	static private final Logger L = LoggerFactory.getLogger( JMXSupport.class );

	@Inject
	private Instance<MBeanServer> svi;

	@Inject
	private Instance<ConfigNode> rootInstance;

	private String domain;

	@Override
	public void register( String domain )
	{
		this.domain = domain;

		final MBeanServer sv = this.svi.get();
		final ConfigNode root = this.rootInstance.get();

		register( sv, this.rootInstance, root );

		this.rootInstance.destroy( root );
		this.svi.destroy( sv );
	}

	private void register( MBeanServer sv, Instance<ConfigNode> rootInstance, ConfigNode node )
	{
		final String item = node.getItem();
		final Map<String, ConfigNode> tree = node.getTree();

		if( tree != null ) {
			tree.values().forEach( n -> register( sv, rootInstance, n ) );
		}
		else {
			final String[] keys = ConfigNode.keys( node.getPath() );
			final StringBuilder name = new StringBuilder();

			for( int k = 0; k < keys.length; k++ ) {
				if( name.length() > 0 ) {
					name.append( "," );
				}

				name.append( format( "%02d=%s", k, keys[k] ) );
			}

			name.insert( 0, this.domain + ":" );

			L.trace( "Register {}", name );

			try {
				sv.registerMBean( new Config( rootInstance, node ), new ObjectName( name.toString() ) );
			}
			catch( final JMException e ) {
				L.warn( format( "Registering %s", name ), e );
			}
		}
	}

	@PreDestroy
	private void preDestroy()
	{
		final MBeanServer sv = this.svi.get();

		try {
			final Set<ObjectName> names = sv.queryNames( new ObjectName( this.domain + ":*" ), null );

			names.forEach( n -> {
				try {
					L.trace( "Unregister {}", n.getCanonicalName() );

					sv.unregisterMBean( n );
				}
				catch( MBeanRegistrationException | InstanceNotFoundException e ) {
					L.warn( format( "Unregistering %s", n.getCanonicalName() ), e );
				}
			} );
		}
		catch( final MalformedObjectNameException e ) {
			L.warn( "Unregistering...", e );
		}
		finally {
			this.svi.destroy( sv );
		}
	}

}
