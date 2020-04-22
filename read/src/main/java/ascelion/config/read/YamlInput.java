package ascelion.config.read;

import static java.lang.String.format;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

class YamlInput extends ConfigInput {
	private final String name;
	private final Map<String, Object> properties;
	private final int priority;

	public YamlInput(Map<String, Object> properties, URL source, int index) {
		this.name = format("%s[%s]", source.toExternalForm(), index);
		this.properties = properties;
		this.priority = Integer.parseInt(Objects.toString(properties.getOrDefault(CONFIG_PRIORITY, DEFAULT_PRIORITY)));
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public int priority() {
		return this.priority;
	}

	@Override
	public void update(Builder bld) {
		update(bld, this.properties);
	}

	private boolean update(Builder bld, Object value) {
		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
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

}
