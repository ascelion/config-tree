package ascelion.config.convert;

import java.util.Optional;
import java.util.Properties;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

public class PropertiesConverter implements ConfigConverter<Properties> {

	@Override
	public Optional<Properties> convert(ConfigNode node) {
		final Properties props = new Properties();
		final String base = node.getPath();
		final int baseLen = base.length() + 1;

		node.getChildren().stream().forEach(child -> {
			final String path = child.getPath();
			final String value = node.root().getValue(path);

			if (value != null) {
				final String key = path.substring(baseLen);

				props.put(key, value);
			}
		});

		return Optional.of(props);
	}
}
