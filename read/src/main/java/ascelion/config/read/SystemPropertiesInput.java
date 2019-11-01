package ascelion.config.read;

import java.util.Map;
import java.util.TreeMap;

import ascelion.config.spi.ConfigInput;

import static java.util.Collections.unmodifiableMap;

class SystemPropertiesInput implements ConfigInput {
	private final Map<String, String> properties = new TreeMap<>();

	SystemPropertiesInput() {
		System.getProperties().forEach((k, v) -> this.properties.put((String) k, (String) v));
	}

	@Override
	public int priority() {
		return 400;
	}

	@Override
	public Map<String, String> properties() {
		return unmodifiableMap(this.properties);
	}
}
