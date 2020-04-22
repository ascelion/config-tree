package ascelion.config.cdi;

import ascelion.config.api.ConfigRoot;
import ascelion.config.core.ConfigProviderImpl;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInputReader;
import ascelion.config.spi.ConverterFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
class CDIConfigProvider extends ConfigProviderImpl {
	@Inject
	private Instance<ConfigInputReader> readers;
	@Inject
	private Instance<ConverterFactory> factories;
	@SuppressWarnings("rawtypes")
	@Inject
	private Instance<ConfigConverter> converters;

	@Produces
	ConfigRoot config() {
		return get();
	}

	@PostConstruct
	private void postConstruct() {
		get();

		initReaders(this.readers);
		initConverters(this.converters);
		initFactories(this.factories);
	}
}
