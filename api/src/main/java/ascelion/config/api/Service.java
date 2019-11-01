package ascelion.config.api;

import java.security.PrivilegedAction;
import java.util.ServiceLoader;

import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class Service<T> {
	private final Class<T> type;

	T load() {
		return doPrivileged((PrivilegedAction<T>) () -> load(classLoader()));
	}

	private ClassLoader classLoader() {
		ClassLoader cld = currentThread().getContextClassLoader();

		if (cld == null) {
			cld = getClass().getClassLoader();
		}

		return cld;
	}

	private T load(ClassLoader cld) {
		T service = null;

		for (final T spi : ServiceLoader.load(this.type, cld)) {
			if (service != null) {
				throw new IllegalStateException("Multiple implementations of " + this.type);
			}

			service = spi;
		}

		if (service == null && cld.getParent() != null) {
			service = load(cld.getParent());
		}
		if (service == null) {
			throw new IllegalStateException("No implementation of " + this.type);
		}

		return service;
	}
}
