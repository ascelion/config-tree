package ascelion.config.core;

import java.io.IOException;
import java.util.ServiceLoader;

import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInputReader;

public class ConfigProviderImpl extends AbstractConfigProvider {

	public ConfigProviderImpl() throws IOException {
		initReaders(ServiceLoader.load(ConfigInputReader.class));
		initConverters(ServiceLoader.load(ConfigConverter.class));
	}
}
