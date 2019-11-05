package ascelion.config.spi;

import java.util.Optional;

import ascelion.config.api.ConfigNode;

public interface ConfigConverter<T> {
	Optional<T> convert(ConfigNode node);
}
