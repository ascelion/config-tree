
package ascelion.cdi.conf;

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
		return Eval.eval( this.node.getValue(), this.root );
	}

}