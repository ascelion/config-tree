
package ascelion.cdi.conf;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import ascelion.cdi.conf.ConfigNode;

public final class Config extends NotificationBroadcasterSupport implements ConfigMBean
{

	static private final String[] NOTIFICATION_TYPES = new String[] {
		AttributeChangeNotification.ATTRIBUTE_CHANGE,
	};

	static private final MBeanNotificationInfo NOTIFICATION = new MBeanNotificationInfo( NOTIFICATION_TYPES, AttributeChangeNotification.class.getName(), "Value changed" );

	private final ConfigNode root;
	private final ConfigNode node;
	private long seq;

	public Config( ConfigNode root, ConfigNode node )
	{
		this.root = root;
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
		return this.node.getValue();
	}

	@Override
	public void setValue( String value )
	{
		final String oldValue = this.node.getValue();

		this.node.setValue( value );

		final Notification n = new AttributeChangeNotification( this, this.seq++, System.currentTimeMillis(), "changed", this.node.getPath(), "java.lang.String", oldValue, value );

		sendNotification( n );
	}

	@Override
	public String getExpandedValue()
	{
		final String value = this.node.getValue();

		if( value == null ) {
			return null;
		}

		return value != null ? Eval.eval( value, this.root ) : null;
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo()
	{
		return new MBeanNotificationInfo[] { NOTIFICATION };
	}

}
