
package ascelion.config.jmx;

import java.util.Objects;
import java.util.function.UnaryOperator;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

class WritableConfigBeanImpl extends ConfigBeanImpl implements WritableConfigBean
{

	static private final String[] NTF_TYPES = new String[] {
		AttributeChangeNotification.ATTRIBUTE_CHANGE,
	};

	static private final MBeanNotificationInfo NTF_INFO = new MBeanNotificationInfo( NTF_TYPES, AttributeChangeNotification.class.getName(), "Value changed" );

	private long ntf_seq;

	WritableConfigBeanImpl( String path, String value, boolean sensitive, UnaryOperator<String> lookup )
	{
		super( path, value, sensitive, lookup );
	}

	@Override
	public void setExpression( String value )
	{
		final String oldValue = this.expression.getExpression();

		if( !Objects.equals( value, oldValue ) ) {

			this.expression.setExpression( value );

			final Notification n = new AttributeChangeNotification( this, this.ntf_seq++, System.currentTimeMillis(), "Configuration changed", getPath(), "java.lang.String", oldValue, value );

			sendNotification( n );
		}
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo()
	{
		return new MBeanNotificationInfo[] { NTF_INFO };
	}

}
