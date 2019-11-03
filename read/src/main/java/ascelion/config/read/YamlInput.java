package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import ascelion.config.api.ConfigProvider.Builder;

import static java.util.stream.Collectors.joining;

import org.yaml.snakeyaml.Yaml;

public class YamlInput extends ResourceInput {
	static private String path(Object... names) {
		return Stream.of(names)
				.map(Object::toString)
				.filter(s -> s.length() > 0)
				.collect(joining("."));
	}

	private final List<Object> documents = new ArrayList<>();
	private final int priority;

	public YamlInput(URL source) throws IOException {
		super(source);

		final Yaml yaml = new Yaml();

		try (InputStream is = source.openStream()) {
			yaml.loadAll(is).forEach(this.documents::add);
		}

		this.priority = this.documents.stream()
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.map(m -> m.get(CONFIG_PRIORITY))
				.filter(Objects::nonNull)
				.map(Number.class::cast)
				.map(Number::intValue)
				.findAny()
				.orElse(DEFAULT_PRIORITY);
	}

	@Override
	public int priority() {
		return this.priority;
	}

	@Override
	public void update(Builder bld) {
		this.documents.forEach(o -> update(bld, o));
	}

	private boolean update(Builder bld, Object value) {
		if (value instanceof Map) {
			final Map<String, Object> map = (Map<String, Object>) value;

			for (final Map.Entry<String, Object> ent : map.entrySet()) {
				bld.child(ent.getKey());

				if (update(bld, ent.getValue())) {
					bld.back();
				}
			}

			return true;
		}

		if (value instanceof Collection) {
			value = ((Collection) value).toArray();
		}
		if (value instanceof Object[]) {
			final Object[] values = (Object[]) value;

			for (final Object v : values) {
				bld.child();

				if (update(bld, v)) {
					bld.back();
				}
			}

			return true;
		}

		final String s = Objects.toString(value, "").trim();

		if (s.isEmpty()) {
			bld.back();
		} else {
			bld.value(s);
		}

		return false;
	}

//	private void add(String prefix, Object value) {
//		if (value instanceof Map) {
//			@SuppressWarnings("unchecked")
//			final Map<String, Object> ms = (Map<String, Object>) value;
//
//			ms.forEach((k, s) -> {
//				add(path(prefix, k), s);
//			});
//		} else if (value instanceof Collection) {
//			final Collection<?> col = (Collection<?>) value;
//			int idx = 0;
//
//			for (final Object v : col) {
//				add(path(prefix, idx++), v);
//			}
//		} else if (value instanceof Object[]) {
//			final Object[] vec = (Object[]) value;
//
//			for (int idx = 0; idx < vec.length; idx++) {
//				add(path(prefix, idx++), vec[idx]);
//			}
//		} else if (value != null) {
//			this.properties.put(prefix, value.toString());
//		}
//	}
}
