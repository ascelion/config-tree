
package ascelion.config.cdi.jmx;

import java.util.Map;
import java.util.TreeMap;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.ObjectName;

import ascelion.config.api.ConfigException;
import ascelion.config.utils.Utils;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

final class JMXTree
{

	private final String domain;
	private final MBeanServer mbs;
	private final Config cf;
	private volatile boolean changed;
	private volatile boolean loading;

	JMXTree( String domain, MBeanServer mbs, Config cf )
	{
		this.domain = domain;
		this.mbs = mbs;
		this.cf = cf;
	}

	boolean isChanged()
	{
		return this.loading ? false : this.changed;
	}

	Map<String, String> query()
	{
		if( this.loading ) {
			return emptyMap();
		}

		try {
			final Map<String, String> map = doQuery();

			if( map == null || map.isEmpty() ) {
				this.loading = true;

				try {
					return buildTree();
				}
				finally {
					this.loading = false;
				}
			}

			return map;
		}
		catch( final MalformedObjectNameException e ) {
			throw new IllegalStateException( e );
		}
		finally {
			this.changed = false;
		}
	}

	private Map<String, String> buildTree() throws MalformedObjectNameException
	{
		this.cf.getPropertyNames().forEach( p -> {
			buildEntry( p, this.cf.getValue( p, String.class ) );
		} );

		return doQuery();
	}

	private Map<String, String> doQuery() throws MalformedObjectNameException
	{
		final Map<String, String> map = new TreeMap<>();

		this.mbs.queryNames( new ObjectName( this.domain + ":*" ), null )
			.forEach( oi -> {
				map.put( pathOf( oi ), valueOf( oi ) );
			} );

		return unmodifiableMap( map );
	}

	private void buildEntry( String path, String value )
	{
		if( System.getProperties().containsKey( path ) ) {
			return;
		}
		if( System.getenv().containsKey( path ) ) {
			return;
		}
		try {
			final ObjectName on = JMXTree.objectName( this.domain, path );
			this.mbs.registerMBean( new ConfigBeanImpl( path, value, this::getValue ), on );
			this.mbs.addNotificationListener( on, this::handleNotification, not -> AttributeChangeNotification.class.isInstance( not ), path );
		}
		catch( InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | InstanceNotFoundException e ) {
			throw new IllegalStateException( path, e );
		}
	}

	private String getValue( String path )
	{
		for( final ConfigSource cs : this.cf.getConfigSources() ) {
			final String val = cs.getValue( path );

			if( val != null ) {
				return val;
			}
		}

		return null;
	}

	private void handleNotification( Notification notification, Object handback )
	{
		this.changed = true;
	}

	private String pathOf( ObjectName on )
	{
		return on.getKeyPropertyListString().replaceAll( "\\d+=", "" ).replace( ",", "." );
	}

	private String valueOf( ObjectName on )
	{
		final ConfigBean cb = JMX.newMBeanProxy( this.mbs, on, ConfigBean.class );

		return cb.getExpression();
	}

	static ObjectName objectName( String domain, String path )
	{
		final String[] keys = Utils.pathNames( path );
		final StringBuilder name = new StringBuilder();

		for( int k = 0; k < keys.length; k++ ) {
			if( name.length() > 0 ) {
				name.append( "," );
			}

			name.append( format( "%02d=%s", k, keys[k] ) );
		}

		name.insert( 0, domain + ":" );

		try {
			return new ObjectName( name.toString() );
		}
		catch( final MalformedObjectNameException e ) {
			throw new ConfigException( name.toString(), e );
		}
	}
}
