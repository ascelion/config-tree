package ascelion.config.api;

import java.util.Optional;

public interface ConfigRoot extends ConfigNode {
	<T> Optional<T> eval(String expression, Class<T> type);
}
