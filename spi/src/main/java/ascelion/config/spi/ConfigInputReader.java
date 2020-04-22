
package ascelion.config.spi;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface ConfigInputReader {
	String RESOURCE_PROP = "ascelion.config.resources";
	String DIRECTORY_PROP = "ascelion.config.directory";

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface Type {
		String value();

		String[] suffixes();
	}

	default Set<String> suffixes() {
		return Optional.ofNullable(getClass().getAnnotation(ConfigInputReader.Type.class))
				.map(ConfigInputReader.Type::suffixes)
				.map(Stream::of)
				.orElseThrow(() -> new IllegalStateException("Must override this method"))
				.collect(toSet());
	}

	default String defaultResource() {
		return "config-tree";
	}

	default Collection<ConfigInput> read() {
		return read(defaultResource());
	}

	Collection<ConfigInput> read(String source);
}
