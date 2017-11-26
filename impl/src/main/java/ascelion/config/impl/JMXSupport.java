
package ascelion.config.impl;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;

import static java.lang.String.format;

public final class JMXSupport
{

	private final MBeanServer mbs;
	private final String domain;

	public JMXSupport( MBeanServer mbs, String domain )
	{
		this.mbs = mbs;
		this.domain = domain;
	}

	public void buildEntries( ConfigNode root )
	{
		final ConfigNodeImpl impl = (ConfigNodeImpl) root;

		impl.tree( false ).values().forEach( this::createEntry );
	}

	private void createEntry( ConfigNodeImpl node )
	{
		if( System.getProperties().containsKey( node.path ) ) {
			return;
		}
		if( System.getenv().containsKey( node.path ) ) {
			return;
		}

		switch( node.item().kindNoEval() ) {
			case NULL:
			case ITEM:
			case LINK:
				final ObjectName name = objectName( node.path );

				try {
					this.mbs.registerMBean( new ConfigBeanImpl( node ), name );
				}
				catch( InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e ) {
					throw new ConfigException( name.getCanonicalName(), e );
				}
			break;

			case NODE:
				buildEntries( node );
		}
	}

	private ObjectName objectName( String path )
	{
		final String[] keys = Utils.keys( path );
		final StringBuilder name = new StringBuilder();

		for( int k = 0; k < keys.length; k++ ) {
			if( name.length() > 0 ) {
				name.append( "," );
			}

			name.append( format( "%02d=%s", k, keys[k] ) );
		}

		name.insert( 0, this.domain + ":" );

		try {
			return new ObjectName( name.toString() );
		}
		catch( final MalformedObjectNameException e ) {
			throw new ConfigException( name.toString(), e );
		}
	}
}
