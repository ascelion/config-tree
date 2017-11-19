
package ascelion.config.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ConfigNode
{

	String getName();

	String getPath();

	String getValue();

	Collection<? extends ConfigNode> getNodes();

	String getValue( String path );

	ConfigNode getNode( String path );

	Map<String, String> asMap();

	Set<String> getKeys();
}
