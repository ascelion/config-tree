package ascelion.config.api;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

public interface ConfigRoot extends ConfigNode {

	default <T> Optional<T> getValue(String path, Class<T> type) {
		return getValue(path, (Type) type);
	}

	default String getValue(String path) {
		return getValue(path, String.class).orElse(null);
	}

	default List<String> getValues(String path) {
		return getValue(path, String[].class)
				.map(Arrays::asList)
				.orElse(emptyList());
	}

	<T> Optional<T> getValue(String path, Type type);
}
