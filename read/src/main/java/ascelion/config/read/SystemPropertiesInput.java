package ascelion.config.read;

import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

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
	public void update(Builder bld) {
		bld.set(this.properties);
	}
}
