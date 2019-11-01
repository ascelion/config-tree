package ascelion.config.read;

import java.util.HashMap;
import java.util.Map;

import ascelion.config.spi.ConfigInput;

class EnvironmentInput implements ConfigInput {
	private final Map<String, String> properties = new HashMap<>();

	EnvironmentInput() {
		this.properties.putAll(System.getenv());
	}

	@Override
	public int priority() {
		return 300;
	}

	@Override
	public Map<String, String> properties() {
		return this.properties;
	}
}
