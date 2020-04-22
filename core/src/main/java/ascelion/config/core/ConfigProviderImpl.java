package ascelion.config.core;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.convert.Converters;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInputReader;
import ascelion.config.spi.ConverterFactory;

import java.io.File;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

public class ConfigProviderImpl extends ConfigProvider {
	static private volatile ConfigRootImpl INSTANCE;
	static private Converters CONVERTERS;

	public static void reset() {
		synchronized (ConfigProviderImpl.class) {
			CONVERTERS = null;
			INSTANCE = null;
		}
	}

	@Override
	public ConfigRoot get() {
		if (INSTANCE != null) {
			return INSTANCE;
		}

		synchronized (ConfigProviderImpl.class) {
			if (INSTANCE != null) {
				return INSTANCE;
			}

			CONVERTERS = new Converters();
			INSTANCE = new ConfigRootImpl(CONVERTERS);

			initConverters(ServiceLoader.load(ConfigConverter.class));
			initFactories(ServiceLoader.load(ConverterFactory.class));
			initReaders(ServiceLoader.load(ConfigInputReader.class));

			return INSTANCE;
		}
	}

	protected final void initReaders(Iterable<ConfigInputReader> readers) {
		final Set<String> skip = new HashSet<>();

		readAll(skip, readers, "");

		final File directory = INSTANCE.getValue(ConfigInputReader.DIRECTORY_PROP, File.class).orElse(null);
		final String[] resources = INSTANCE.getValue(ConfigInputReader.RESOURCE_PROP, String[].class).orElseGet(() -> new String[0]);

		for (final String resource : resources) {
			if (directory != null) {
				readAll(skip, readers, new File(directory, resource).getAbsolutePath());
			}

			readAll(skip, readers, resource);
		}
	}

	protected final void initFactories(Iterable<ConverterFactory> factories) {
		factories.forEach(CONVERTERS::register);
	}

	protected final void initConverters(@SuppressWarnings("rawtypes") Iterable<ConfigConverter> converters) {
		converters.forEach(CONVERTERS::register);
	}

	private void readAll(Set<String> skip, Iterable<ConfigInputReader> readers, String source) {
		if (skip.add(source)) {
			for (final ConfigInputReader rd : readers) {
				INSTANCE.addConfigInputs(source.isEmpty() ? rd.read() : rd.read(source));
			}
		}
	}
}
