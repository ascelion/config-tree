package ascelion.config.api;

import java.util.Map;

public abstract class ConfigProvider {
	public interface Builder {
		Builder child();

		Builder child(String path);

		Builder value(String value);

		Builder set(Map<String, String> properties);

		Builder set(String path, String value);

		Builder back();

		ConfigRoot get();
	}

	static public ConfigRoot root() {
		return new Service<>(ConfigProvider.class).load().get();
	}

	protected abstract ConfigRoot get();
}
