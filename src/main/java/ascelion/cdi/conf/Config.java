
package ascelion.cdi.conf;

import java.util.LinkedHashSet;

import ascelion.cdi.conf.ExpressionRules.Rule;
import ascelion.shared.cdi.conf.ConfigNode;

public final class Config implements ConfigMBean
{

	private final ConfigNode root;
	private final ConfigNode node;

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
		this.node.setValue( value );
	}

	@Override
	public String getExpandedValue()
	{
		final Rule rule = ExpressionRules.parse( this.node.getValue() );

		return rule.evaluate( this.root, new LinkedHashSet<>() );
	}

}
