
package ascelion.config.cdi.jmx;

import java.util.Objects;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import lombok.Getter;

class ConfigBeanImpl extends NotificationBroadcasterSupport implements ConfigBean
{

	static private final String[] NTF_TYPES = new String[] {
		AttributeChangeNotification.ATTRIBUTE_CHANGE,
	};

	static private final MBeanNotificationInfo NTF_INFO = new MBeanNotificationInfo( NTF_TYPES, AttributeChangeNotification.class.getName(), "Value changed" );

	@Getter
	private final String path;
	@Getter
	private String value;
	private long ntf_seq;

	ConfigBeanImpl( String path, String value )
	{
		this.path = path;
		this.value = value;
	}

	@Override
	public void setValue( String value )
	{
		if( !Objects.equals( value, this.value ) ) {
			final String oldValue = this.value;

			this.value = value;

			final Notification n = new AttributeChangeNotification( this, this.ntf_seq++, System.currentTimeMillis(), "Configuration changed", this.path, "java.lang.String", oldValue, value );

			sendNotification( n );
		}
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo()
	{
		return new MBeanNotificationInfo[] { NTF_INFO };
	}
}
