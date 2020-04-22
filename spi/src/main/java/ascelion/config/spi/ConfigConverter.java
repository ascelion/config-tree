
package ascelion.config.spi;

import ascelion.config.api.ConfigNode;

import java.util.Optional;

public interface ConfigConverter<T>
{

	Optional<T> convert( ConfigNode node );
}
