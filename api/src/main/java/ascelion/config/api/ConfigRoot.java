package ascelion.config.api;

import java.lang.reflect.Type;
import java.util.Optional;

public interface ConfigRoot extends ConfigNode {
	<T> Optional<T> eval(String expression, Type type);

	<T> Optional<T> eval(String expression, Class<T> type);
}
