
package ascelion.config.cdi.jmx;

import java.util.Objects;
import java.util.function.UnaryOperator;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import ascelion.config.utils.Expression;

import lombok.Getter;

class ConfigBeanImpl extends NotificationBroadcasterSupport implements ConfigBean
{

	static private final String[] NTF_TYPES = new String[] {
		AttributeChangeNotification.ATTRIBUTE_CHANGE,
	};

	static private final MBeanNotificationInfo NTF_INFO = new MBeanNotificationInfo( NTF_TYPES, AttributeChangeNotification.class.getName(), "Value changed" );

	@Getter
	private final String path;
	private final Expression expression;
	private long ntf_seq;

	ConfigBeanImpl( String path, String value, UnaryOperator<String> lookup )
	{
		this.path = path;
		this.expression = new Expression( lookup );

		this.expression.setExpression( value );
	}

	@Override
	public void setExpression( String value )
	{
		final String oldValue = this.expression.getExpression();

		if( !Objects.equals( value, oldValue ) ) {

			this.expression.setExpression( value );

			final Notification n = new AttributeChangeNotification( this, this.ntf_seq++, System.currentTimeMillis(), "Configuration changed", this.path, "java.lang.String", oldValue, value );

			sendNotification( n );
		}
	}

	@Override
	public String getExpression()
	{
		return this.expression.getExpression();
	}

	@Override
	public String getValue()
	{
		this.expression.expire();
		return this.expression.getValue();
	}

	@Override
	public String getDefaultValue()
	{
		this.expression.expire();
		return this.expression.getDefValue();
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo()
	{
		return new MBeanNotificationInfo[] { NTF_INFO };
	}
}
