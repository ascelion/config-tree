
package ascelion.config.jmx;

import java.util.function.UnaryOperator;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;

class WritableConfigBeanImpl extends ConfigBeanImpl implements WritableConfigBean
{

	static private final String[] NTF_TYPES = new String[] {
		AttributeChangeNotification.ATTRIBUTE_CHANGE,
	};

	static private final MBeanNotificationInfo NTF_INFO = new MBeanNotificationInfo( NTF_TYPES, AttributeChangeNotification.class.getName(), "Value changed" );

	WritableConfigBeanImpl( String path, boolean sensitive, UnaryOperator<String> lookup )
	{
		super( path, sensitive, lookup );
	}

	@Override
	public void setExpression( String value )
	{
		super.setExpression( value );
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo()
	{
		return new MBeanNotificationInfo[] { NTF_INFO };
	}

}
