package ascelion.config.api;

import java.util.Optional;

import ascelion.config.spi.ConfigRootProvider;

public interface ConfigRoot extends ConfigNode {
	class Instance {
		static private volatile ConfigRoot ROOT;
	}

	static ConfigRoot get() {
		if( Instance.ROOT != null ) {
			return Instance.ROOT;
		}

		synchronized( Instance.class ) {
			if( Instance.ROOT != null ) {
				return Instance.ROOT;
			}

			return Instance.ROOT = ConfigRootProvider.load();
		}
	}

	static ConfigRoot load() {
		return Instance.ROOT = ConfigRootProvider.load();
	}

	<T> Optional<T> getValue(String expression, Class<T> type);
}
