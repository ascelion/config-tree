package ascelion.config.spi;

import java.security.PrivilegedAction;
import java.util.ServiceLoader;

import ascelion.config.api.ConfigRoot;

import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;

public abstract class ConfigRootProvider {
	public static ConfigRoot load() {
		return doPrivileged((PrivilegedAction<ConfigRoot>) () -> load(classLoader()));
	}

	static private ConfigRoot load(ClassLoader cld) {
		ConfigRootProvider provider = null;

		for (final ConfigRootProvider spi : ServiceLoader.load(ConfigRootProvider.class, cld)) {
			if (provider != null) {
				throw new IllegalStateException("No implementation");
			}

			provider = spi;
		}

		if (provider == null && cld.getParent() != null) {
			provider = (ConfigRootProvider) load(cld.getParent());
		}
		if (provider == null) {
			throw new IllegalStateException("No implementation");
		}

		return provider.get();
	}

	static private ClassLoader classLoader() {
		ClassLoader cld = currentThread().getContextClassLoader();

		if (cld == null) {
			cld = ConfigRootProvider.class.getClassLoader();
		}

		return cld;
	}

	protected abstract ConfigRoot get();
}

