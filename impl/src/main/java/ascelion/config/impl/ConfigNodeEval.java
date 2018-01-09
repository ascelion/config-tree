
package ascelion.config.impl;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;

import ascelion.config.api.ConfigNode;

import static ascelion.config.impl.Utils.path;

class ConfigNodeEval implements ConfigNode
{

	private final ConfigNode root;
	private final String name;
	private final String path;
	private Kind kind;
	private Object item;

	ConfigNodeEval()
	{
		this.root = this;
		this.name = null;
		this.path = null;
	}

	private ConfigNodeEval( ConfigNodeEval parent, String name )
	{
		this.name = name;
		this.path = path( path( parent ), name );
		this.root = parent.root;

		parent.tree( true ).put( name, this );
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getPath()
	{
		return this.path;
	}

	@Override
	public Kind getKind()
	{
		return this.kind;
	}

	@Override
	public <T> T getValue()
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
	public <T> T getValue( String path )
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

	Map<String, ConfigNode> tree( boolean create )
	{
		return null;
	}

}
