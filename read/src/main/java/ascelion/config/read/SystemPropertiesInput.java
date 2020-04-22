package ascelion.config.read;

import static java.lang.String.format;
import static java.security.AccessController.doPrivileged;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

import java.security.PrivilegedAction;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class SystemPropertiesInput extends ConfigInput {
	@Override
	public int priority() {
		return 400;
	}

	@Override
	public void update(Builder bld) {
		log.trace(format("Updating builder from %s", name()));

		properties().forEach((k, v) -> bld.set((String) k, (String) v));
	}

	private Properties properties() {
		return doPrivileged((PrivilegedAction<Properties>) () -> System.getProperties());
	}
}
