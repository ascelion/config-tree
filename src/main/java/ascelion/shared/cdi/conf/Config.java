
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.Instance;

public class Config implements ConfigMBean
{

	private final Instance<ConfigNode> rootInstance;
	private final ConfigNode node;

	Config( Instance<ConfigNode> rootInstance, ConfigNode node )
	{
		this.rootInstance = rootInstance;
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
		return this.node.getItem();
	}

	@Override
	public void setValue( String value )
	{
		this.node.set( value );
	}

	@Override
	public String getExpandedValue()
	{
		final ConfigNode root = this.rootInstance.get();
		try {
			return new Expander( this.node.getItem(), x -> root.getItem( x ) ).expand();
		}
		finally {
			this.rootInstance.destroy( root );
		}
	}

}
