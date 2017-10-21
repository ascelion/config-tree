
package ascelion.shared.cdi.conf;

public class Config implements ConfigMXBean
{

	private final ConfigNode node;

	Config( ConfigNode node )
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
		return this.node.getItem();
	}

	@Override
	public void setValue( String value )
	{
		this.node.set( value );
	}

}
