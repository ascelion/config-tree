package ascelion.config.spi;

import static java.lang.String.format;

import ascelion.config.api.ConfigProvider;

import java.util.Objects;

public abstract class ConfigInput implements Comparable<ConfigInput> {
	static public final String CONFIG_PRIORITY = "config_ordinal";
	static public final int DEFAULT_PRIORITY = 100;

	static public int priority(String value) {
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (final NumberFormatException ignored) {
			}
		}

		return DEFAULT_PRIORITY;
	}

	public String name() {
		return getClass().getSimpleName();
	}

	@Override
	public int hashCode() {
		return Objects.hash(name());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		final ConfigInput that = (ConfigInput) obj;

		return Objects.equals(name(), that.name());
	}

	@Override
	public final String toString() {
		return format("%s[%s]", getClass().getSimpleName(), name());
	}

	@Override
	public final int compareTo(ConfigInput o) {
		return Integer.compare(priority(), o.priority());
	}

	abstract public int priority();

	abstract public void update(ConfigProvider.Builder bld);
}
