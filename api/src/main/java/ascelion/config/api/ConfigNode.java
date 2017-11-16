
package ascelion.config.api;

import java.util.Collection;
import java.util.Map;

public interface ConfigNode
{

	String getName();

	String getPath();

	String getValue( boolean expand );

	default String getValue()
	{
		return getValue( true );
	}

	Collection<? extends ConfigNode> getChildren( boolean expand );

	default Collection<? extends ConfigNode> getChildren()
	{
		return getChildren( true );
	}

	ConfigNode getNode( String path );

	Map<String, String> asMap();
}
