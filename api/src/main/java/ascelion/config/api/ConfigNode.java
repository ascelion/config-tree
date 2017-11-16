
package ascelion.config.api;

import java.util.Collection;
import java.util.Map;

public interface ConfigNode
{

	String getName();

	String getPath();

	String getValue();

	Collection<? extends ConfigNode> getChildren();

	ConfigNode getNode( String path );

	Map<String, String> asMap();
}
