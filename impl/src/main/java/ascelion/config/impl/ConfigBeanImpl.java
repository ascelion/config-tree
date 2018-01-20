
package ascelion.config.impl;

import java.util.Objects;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import ascelion.config.api.ConfigParseException;

class ConfigBeanImpl extends NotificationBroadcasterSupport implements ConfigBean
{

	static private final String[] NTF_TYPES = new String[] {
		AttributeChangeNotification.ATTRIBUTE_CHANGE,
	};

	static private final MBeanNotificationInfo NTF_INFO = new MBeanNotificationInfo( NTF_TYPES, AttributeChangeNotification.class.getName(), "Value changed" );

	private final ConfigNodeImpl node;
	private long ntf_seq;

	ConfigBeanImpl( ConfigNodeImpl node )
	{
		this.node = node;
	}

	@Override
	public String getName()
	{
		return this.node.getName();
	}

	@Override
	public String getPath()
	{
		return this.node.getPath();
	}

	@Override
	public String getValue()
	{
		return Objects.toString( this.node.getValue(), null );
	}

	@Override
	public String getRawValue()
	{
		return Objects.toString( this.node.getRawValue(), null );
	}

	@Override
	public void setValue( String value )
	{
		final String oldValue = getRawValue();

		if( !Objects.equals( value, oldValue ) ) {
			try {
				this.node.setValue( value );
			}
			catch( final ConfigParseException e ) {
				this.node.setValue( value.replace( ":", "\\:" ) );
			}
			finally {
				final Notification n = new AttributeChangeNotification( this, this.ntf_seq++, System.currentTimeMillis(), "Configuration changed", this.node.getPath(), "java.lang.String", oldValue, value );

				sendNotification( n );
			}
		}
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo()
	{
		return new MBeanNotificationInfo[] { NTF_INFO };
	}
}
