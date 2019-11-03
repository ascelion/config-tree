package ascelion.config.api;

import java.util.Map;

public interface ConfigProvider {
	static ConfigProvider load() {
		return new Service<>(ConfigProvider.class).load();
	}

	public interface Builder {
		Builder child();

		Builder child(String path);

		Builder value(String value);

		Builder set(Map<String, String> properties);

		Builder set(String path, String value);

		Builder back();

		ConfigRoot get();
	}

	ConfigRoot get();
}
