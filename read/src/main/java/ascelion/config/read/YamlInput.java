package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;

import org.yaml.snakeyaml.Yaml;

public class YamlInput extends ResourceInput {
	static private String path(Object... names) {
		return Stream.of(names)
				.map(Object::toString)
				.filter(s -> s.length() > 0)
				.collect(joining("."));
	}

	private final Map<String, String> properties = new TreeMap<>();

	public YamlInput(URL source) throws IOException {
		super(source);
		final Yaml yaml = new Yaml();

		try (InputStream is = source.openStream()) {
			yaml.loadAll(is)
					.forEach(o -> {
						add("", o);
					});
		}
	}

	@Override
	public Map<String, String> properties() {
		return unmodifiableMap(this.properties);
	}

	private void add(String prefix, Object value) {
		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String, Object> ms = (Map<String, Object>) value;

			ms.forEach((k, s) -> {
				add(path(prefix, k), s);
			});
		} else if (value instanceof Collection) {
			final Collection<?> col = (Collection<?>) value;
			int idx = 0;

			for (final Object v : col) {
				add(path(prefix, idx++), v);
			}
		} else if (value instanceof Object[]) {
			final Object[] vec = (Object[]) value;

			for (int idx = 0; idx < vec.length; idx++) {
				add(path(prefix, idx++), vec[idx]);
			}
		} else if (value != null) {
			this.properties.put(prefix, value.toString());
		}
	}
}
