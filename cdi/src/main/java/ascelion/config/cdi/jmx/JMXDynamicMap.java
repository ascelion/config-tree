
package ascelion.config.cdi.jmx;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.ObjectName;

import ascelion.config.api.ConfigException;
import ascelion.config.utils.Utils;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.synchronizedMap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

class JMXDynamicMap extends AbstractMap<String, String>
{

	private final MBeanServer mbs;
	private final String domain;
	private final UnaryOperator<String> init;
	private final Map<String, String> delegate = synchronizedMap( new TreeMap<>() );
	private boolean loading;
	@Getter( AccessLevel.PACKAGE )
	@Setter( AccessLevel.PACKAGE )
	private boolean changed;

	JMXDynamicMap( MBeanServer mbs, String domain, UnaryOperator<String> init )
	{
		this.mbs = mbs;
		this.domain = domain;
		this.init = init;
	}

	@Override
	public Set<Map.Entry<String, String>> entrySet()
	{
		if( this.loading ) {
			return emptySet();
		}

		return this.delegate.entrySet();
	}

	@Override
	public String get( Object key )
	{
		if( this.loading ) {
			return null;
		}

		if( containsKey( key ) ) {
			return this.delegate.get( key );
		}
		else {
			return null;
		}
	}

	@Override
	public boolean containsKey( Object key )
	{
		if( this.delegate.containsKey( key ) ) {
			return true;
		}
		if( this.loading ) {
			return false;
		}

		Objects.requireNonNull( key, "Key cannot be null" );

		final String path = key.toString();
		final ObjectName on = objectName( path );

		initObject( path, on );

		return true;
	}

	private ObjectName objectName( String path )
	{
		final String[] keys = Utils.pathNames( path );
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

	private void initObject( String path, ObjectName on )
	{
		final String cv = getValue( path );

		if( cv != null ) {
			try {
				this.mbs.registerMBean( new ConfigBeanImpl( path, cv, this.init ), on );
				this.mbs.addNotificationListener( on, this::handleNotification, not -> AttributeChangeNotification.class.isInstance( not ), path );

				this.delegate.put( path, cv );
			}
			catch( InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | InstanceNotFoundException e ) {
				throw new IllegalStateException( path, e );
			}
		}
	}

	private void handleNotification( Notification notification, Object handback )
	{
		this.changed = true;

		final AttributeChangeNotification acn = (AttributeChangeNotification) notification;

		this.delegate.put( handback.toString(), acn.getNewValue().toString() );
	}

	private String getValue( String propertyName )
	{
		this.loading = true;

		try {
			return this.init.apply( propertyName );
		}
		finally {
			this.loading = false;
		}
	}
}
