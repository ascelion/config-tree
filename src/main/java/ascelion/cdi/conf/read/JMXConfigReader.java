
package ascelion.cdi.conf.read;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMX;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import ascelion.cdi.conf.Config;
import ascelion.cdi.conf.ConfigMBean;
import ascelion.cdi.conf.ConfigNodeImpl;
import ascelion.shared.cdi.conf.ConfigException;
import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ConfigSource.Type( value = "JMX" )
@ApplicationScoped
public class JMXConfigReader implements ConfigReader, NotificationListener
{

	static private final Logger L = LoggerFactory.getLogger( JMXConfigReader.class );

	private ConfigSource source;

	@Inject
	private Instance<MBeanServer> mbsi;

	@Inject
	private Event<ConfigSource> event;

	private MBeanServer mbs;

	@Override
	public void readConfiguration( ConfigSource source, ConfigNode root )
	{
		if( this.source == null ) {
			this.source = source;
		}

		try {
			root.asMap( x -> x ).forEach( ( k, v ) -> setValue( this.mbs, root, k, v ) );
		}
		finally {
			this.mbsi.destroy( this.mbs );
		}
	}

	@Override
	public boolean enabled()
	{
		return this.mbs != null;
	}

	private void setValue( MBeanServer mbs, ConfigNode root, String path, String value )
	{
		final ObjectName name = objectName( path );

		if( mbs.isRegistered( name ) ) {
			final ConfigMBean o = JMX.newMBeanProxy( mbs, name, ConfigMBean.class );
			final String v = o.getValue();

			if( v != null ) {
				root.setValue( path, v );
			}
		}
		else {
			try {
				mbs.registerMBean( new Config( root, root.getNode( path ) ), name );
				mbs.addNotificationListener( name, this, null, null );
			}
			catch( final JMException e ) {
				L.warn( format( "Registering %s", name ), e );
			}
		}
	}

	private ObjectName objectName( String path )
	{
		final String[] keys = ConfigNodeImpl.keys( path );
		final StringBuilder name = new StringBuilder();

		for( int k = 0; k < keys.length; k++ ) {
			if( name.length() > 0 ) {
				name.append( "," );
			}

			name.append( format( "%02d=%s", k, keys[k] ) );
		}

		name.insert( 0, this.source.value() + ":" );

		try {
			return new ObjectName( name.toString() );
		}
		catch( final MalformedObjectNameException e ) {
			throw new ConfigException( name.toString(), e );
		}
	}

	@PostConstruct
	private void postConstruct()
	{
		try {
			this.mbs = this.mbsi.get();
		}
		catch( final UnsatisfiedResolutionException e ) {
			;
		}
	}

	@PreDestroy
	private void preDestroy()
	{
		if( this.mbs == null ) {
			return;
		}

		try {
			final Set<ObjectName> names = this.mbs.queryNames( new ObjectName( this.source.value() + ":*" ), null );

			names.forEach( n -> {
				try {
					L.trace( "Unregister {}", n.getCanonicalName() );

					this.mbs.unregisterMBean( n );
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
			this.mbsi.destroy( this.mbs );
		}
	}

	@Override
	public void handleNotification( Notification notification, Object handback )
	{
		this.event.fire( this.source );
	}
}
