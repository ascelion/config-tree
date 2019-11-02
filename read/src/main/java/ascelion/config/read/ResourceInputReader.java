
package ascelion.config.read;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

import static java.lang.Thread.currentThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceInputReader implements ConfigInputReader {

	static private final Logger LOG = LoggerFactory.getLogger(ResourceInputReader.class);

	@Override
	public final Collection<ConfigInput> read(String source) {
		final Collection<ConfigInput> inputs = new ArrayList<>();

		final int suffixIdx = source.lastIndexOf('.');

		if (suffixIdx < 0) {
			final Collection<String> suffixes = suffixes();

			if (suffixes.size() > 0) {
				for (final String suffix : suffixes) {
					collect(source + "." + suffix, inputs);
				}

				return inputs;
			}
		}

		collect(source, inputs);

		return inputs;
	}

	private void collect(String source, Collection<ConfigInput> inputs) {
		final File file = new File(source);

		if (file.exists()) {
			try {
				LOG.debug("Reading {}", file.getAbsolutePath());

				inputs.add(read(file.toURI().toURL()));
			} catch (final IOException e) {
				LOG.warn(file.getAbsolutePath(), e);
			}
		}

		Enumeration<URL> resources;

		try {
			resources = currentThread().getContextClassLoader().getResources(source);
		} catch (final IOException e) {
			LOG.warn(source, e);

			return;
		}

		while (resources.hasMoreElements()) {
			final URL resource = resources.nextElement();

			try {
				LOG.debug("Reading {}", resource);

				inputs.add(read(resource));
			} catch (final IOException e) {
				LOG.warn(resource.toExternalForm(), e);
			}
		}
	}

	protected abstract ConfigInput read(URL source) throws IOException;
}
