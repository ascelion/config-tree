package ascelion.config.read;

import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConfigInputReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.yaml.snakeyaml.Yaml;

@ConfigInputReader.Type(value = "YML", suffixes = { "yml", "yaml" })
public class YamlInputReader extends ResourceInputReader {

	@Override
	protected Collection<ConfigInput> readFrom(URL source) throws IOException {
		final Collection<ConfigInput> inputs = new ArrayList<>();

		try (InputStream is = source.openStream()) {
			final Yaml yaml = new Yaml();

			StreamSupport.stream(yaml.loadAll(is).spliterator(), false)
					.filter(Map.class::isInstance)
					.map(Map.class::cast)
					.forEach(m -> inputs.add(new YamlInput(m, source, inputs.size())));
		}

		return inputs;
	}
}
