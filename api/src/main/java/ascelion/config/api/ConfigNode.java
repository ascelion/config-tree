
package ascelion.config.api;

import java.util.Collection;
import java.util.Optional;

public interface ConfigNode {

	String getName();

	String getPath();

	Optional<String> getValue();

//	Optional<ConfigNode> getChild(String name);

	Collection<ConfigNode> getChildren();
}
