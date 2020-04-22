package ascelion.config.read;

import static java.lang.String.format;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PropertiesInput extends ResourceInput {
	private final Map<String, String> properties = new TreeMap<>();

	public PropertiesInput(URL source) throws IOException {
		super(source);

		try (final InputStream is = source.openStream()) {
			final Properties p = new Properties();

			p.load(is);
			p.forEach((k, v) -> this.properties.put((String) k, (String) v));
		}
	}

	@Override
	public int priority() {
		return ConfigInput.priority(this.properties.get(CONFIG_PRIORITY));
	}

	@Override
	public void update(Builder bld) {
		log.trace(format("Updating builder from %s", name()));

		bld.set(this.properties);
	}

}
