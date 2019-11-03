package ascelion.config.spi;

import ascelion.config.api.ConfigProvider;

public interface ConfigInput extends Comparable<ConfigInput> {
	String CONFIG_PRIORITY = "config_ordinal";
	int DEFAULT_PRIORITY = 100;

	static int priority(String value) {

		if (value != null) {
			try {
				return Integer.parseInt(value);
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

	int priority();

	void update(ConfigProvider.Builder bld);
}
