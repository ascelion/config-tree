
package ascelion.config.jmx;

import java.util.Map;
import java.util.TreeMap;

import javax.management.*;

import ascelion.config.api.ConfigException;
import ascelion.config.eclipse.ext.ConfigExt;
import ascelion.config.utils.Utils;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import org.eclipse.microprofile.config.spi.ConfigSource;

final class JMXTree
{

	enum State
	{
		INIT,
		LOAD,
		DONE,
	}

	private final String domain;
	private final MBeanServer mbs;
	private final ConfigExt cf;
	private final Map<String, JMXConfig> jmxConfigs;
	private volatile boolean changed;
	private volatile State state = State.INIT;

	JMXTree( String domain, MBeanServer mbs, ConfigExt cf, Map<String, JMXConfig> jmxConfigs )
	{
		this.domain = domain;
		this.mbs = mbs;
		this.cf = cf;
		this.jmxConfigs = jmxConfigs;
	}

	boolean isChanged()
	{
		return this.state == State.DONE ? this.changed : false;
	}

	Map<String, String> query()
	{
		switch( this.state ) {
			case LOAD:
				return emptyMap();

			case INIT:
				this.state = State.LOAD;

				this.cf.getPropertyNames()
					.forEach( this::buildEntry );

				this.state = State.DONE;

			case DONE:
				try {
					return doQuery();
				}
				finally {
					this.changed = false;
				}

			default:
				throw new AssertionError( "UNREACHABLE CODE!!!" );
		}
	}

	private Map<String, String> doQuery()
	{
		final ObjectName wc;

		try {
			wc = new ObjectName( this.domain + ":*" );
		}
		catch( final MalformedObjectNameException e ) {
			throw new IllegalStateException( e );
		}

		return ConfigBeanImpl.unprotected( () -> {
			final Map<String, String> map = new TreeMap<>();

			this.mbs.queryNames( wc, Query.eq( Query.attr( "Modified" ), Query.value( true ) ) )
				.forEach( oi -> {
					final ConfigBean cb = JMX.newMBeanProxy( this.mbs, oi, ConfigBean.class );

					map.put( pathOf( oi ), cb.getExpression() );
				} );

			return unmodifiableMap( map );
		} );
	}

	private void buildEntry( String path )
	{
		if( !this.jmxConfigs.containsKey( path ) ) {
			if( System.getProperties().containsKey( path ) ) {
				return;
			}
			if( System.getenv().containsKey( path ) ) {
				return;
			}
		}

		try {
			final ObjectName on = JMXTree.objectName( this.domain, path );
			final JMXConfig jo = this.jmxConfigs.get( path );

			if( jo != null && jo.writable() ) {
				this.mbs.registerMBean( new WritableConfigBeanImpl( path, jo.sensitive(), this::getValue ), on );
			}
			else {
				this.mbs.registerMBean( new ConfigBeanImpl( path, jo != null ? jo.sensitive() : false, this::getValue ), on );
			}

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
