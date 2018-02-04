
package ascelion.config.jmx;

import java.util.function.UnaryOperator;

import javax.management.NotificationBroadcasterSupport;

import ascelion.config.utils.Expression;

import lombok.Getter;

class ConfigBeanImpl extends NotificationBroadcasterSupport implements ConfigBean
{

	@Getter
	private final String path;
	private final boolean sensitive;
	protected final Expression expression;

	ConfigBeanImpl( String path, String value, boolean sensitive, UnaryOperator<String> lookup )
	{
		this.path = path;
		this.sensitive = sensitive;
		this.expression = new Expression( lookup );

		this.expression.setExpression( value );
	}

	@Override
	public String getExpression()
	{
		return this.expression.getExpression();
	}

	@Override
	public String getValue()
	{
		if( this.sensitive ) {
			return "**************";
		}

		this.expression.expire();
		return this.expression.getValue();
	}

	@Override
	public String getDefaultValue()
	{
		if( this.sensitive ) {
			return "**************";
		}

		this.expression.expire();
		return this.expression.getDefValue();
	}
}
