
package ascelion.config.impl;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;

import ascelion.config.api.ConfigNode;

public class ConfigNodeEclipse implements ConfigNode
{

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public String getPath()
	{
		return null;
	}

	@Override
	public String getValue()
	{
		return null;
	}

	@Override
	public String getRawValue()
	{
		return null;
	}

	@Override
	public Collection<ConfigNode> getNodes()
	{
		return null;
	}

	@Override
	public Set<String> getKeys()
	{
		return null;
	}

	@Override
	public ConfigNode getNode( String path )
	{
		return null;
	}

	@Override
	public String getValue( String path )
	{
		return null;
	}

	@Override
	public void addChangeListener( PropertyChangeListener pcl )
	{
	}

	@Override
	public void removeChangeListener( PropertyChangeListener pcl )
	{
	}
}
