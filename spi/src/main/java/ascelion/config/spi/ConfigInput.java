package ascelion.config.spi;

import java.util.Map;

public interface ConfigInput extends Comparable<ConfigInput> {
	String CONFIG_PRIORITY = "config_ordinal";
	int DEFAULT_PRIORITY = 100;

	default int priority() {
		final String configOrdinal = properties().get(CONFIG_PRIORITY);

		if (configOrdinal != null) {
			try {
				return Integer.parseInt(configOrdinal);
			} catch (final NumberFormatException ignored) {
			}
		}

		return DEFAULT_PRIORITY;
	}

	default String name() {
		return getClass().getSimpleName();
	}

	@Override
	default int compareTo(ConfigInput o) {
		return Integer.compare(priority(), o.priority());
	}

	Map<String, String> properties();
}
