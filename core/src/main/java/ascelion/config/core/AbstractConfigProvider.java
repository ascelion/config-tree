package ascelion.config.core;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.convert.Converters;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInputReader;

public abstract class AbstractConfigProvider extends ConfigProvider {
	private final Converters converters = new Converters();
	private final ConfigRootImpl root = new ConfigRootImpl(this.converters);

	@Override
	public final ConfigRoot get() {
		return this.root;
	}

	protected final void initReaders(Iterable<ConfigInputReader> readers) throws IOException {
		final Set<String> skip = new HashSet<>();

		readAll(skip, readers, null);

		final File directory = this.root.eval(ConfigInputReader.DIRECTORY_PROP, File.class).orElse(null);
		final String[] resources = this.root.eval(ConfigInputReader.RESOURCE_PROP, String[].class).orElseGet(() -> new String[0]);

		for (final String resource : resources) {
			if (directory != null) {
				readAll(skip, readers, new File(directory, resource).getAbsolutePath());
			}

			readAll(skip, readers, resource);
		}
	}

	protected final void initConverters(@SuppressWarnings("rawtypes") Iterable<ConfigConverter> converters) {
		converters.forEach(this.converters::register);
	}

	private void readAll(Set<String> skip, Iterable<ConfigInputReader> readers, String source) throws IOException {
		if (skip.add(source)) {
			for (final ConfigInputReader rd : readers) {
				this.root.addConfigInputs(source != null ? rd.read(source) : rd.read());
			}
		}
	}
}