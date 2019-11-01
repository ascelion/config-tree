
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

public abstract class ResourceInputReader implements ConfigInputReader {

	@Override
	public final Collection<ConfigInput> read(String source) throws IOException {
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

	private void collect(String source, Collection<ConfigInput> inputs) throws IOException {
		final File file = new File(source);

		if (file.exists()) {
			inputs.add(read(file.toURI().toURL()));
		}

		final Enumeration<URL> resources = currentThread().getContextClassLoader().getResources(source);

		while (resources.hasMoreElements()) {
			inputs.add(read(resources.nextElement()));
		}
	}

	protected abstract ConfigInput read(URL source) throws IOException;
}
