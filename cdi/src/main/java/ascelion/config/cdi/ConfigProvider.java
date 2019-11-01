package ascelion.config.cdi;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ascelion.config.api.ConfigRoot;
import ascelion.config.core.AbstractConfigProvider;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInputReader;

class ConfigProvider extends AbstractConfigProvider {
	@Inject
	private Instance<ConfigInputReader> readers;
	@Inject
	private Instance<ConfigConverter> converters;

	@Produces
	ConfigRoot config() {
		return get();
	}

	@PostConstruct
	private void postConstruct() throws IOException {
		initReaders(this.readers);
		initConverters(this.converters);
	}

}
