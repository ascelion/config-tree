package ascelion.config.api;

public abstract class ConfigProvider {
	static private volatile ConfigProvider INSTANCE;

	public static ConfigProvider load() {
		if (INSTANCE != null) {
			return INSTANCE;
		}

		synchronized (ConfigProvider.class) {
			if (INSTANCE != null) {
				return INSTANCE;
			}

			return INSTANCE = new Service<>(ConfigProvider.class).load();
		}
	}

	public abstract ConfigRoot get();
}
