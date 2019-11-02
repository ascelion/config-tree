package ascelion.config.cdi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ascelion.config.api.ConfigRoot;
import ascelion.config.core.ConfigProviderImpl;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInputReader;

@ApplicationScoped
class CDIConfigProvider extends ConfigProviderImpl {
	@Inject
	private Instance<ConfigInputReader> readers;
	@Inject
	private Instance<ConfigConverter> converters;

	@Produces
	ConfigRoot config() {
		return get();
	}

	@PostConstruct
	private void postConstruct() {
		initReaders(this.readers);
		initConverters(this.converters);
	}

}
