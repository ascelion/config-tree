
package ascelion.config.api;

import java.util.Collection;
import java.util.Optional;

public interface ConfigNode
{

	ConfigRoot root();

	String getName();

	String getPath();

	Optional<String> getValue();

	Optional<ConfigNode> getNode( String path );

	Collection<ConfigNode> getChildren();
}
