package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableMap;

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
	public Map<String, String> properties() {
		return unmodifiableMap(this.properties);
	}

}
