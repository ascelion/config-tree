package ascelion.config.convert;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConverterFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class PropertiesConverter implements ConfigConverter<Properties> {

	private final ConverterFactory cvf;

	@Override
	public Optional<Properties> convert(ConfigNode node) {
		final Collection<ConfigNode> children = node.getChildren();
		final Properties map = new Properties();
		final String base = node.getPath();
		final int baseLen = base.length() + 1;

		fillMap(map, children, baseLen);

		return Optional.of(map);
	}

	private void fillMap(Properties map, Collection<ConfigNode> children, int baseLen) {
		children.forEach(child -> fillMap(map, child, baseLen));
	}

	private void fillMap(Properties map, ConfigNode child, int baseLen) {
		final Optional<String> opt = this.cvf.<String>get(String.class).convert(child);

		if (opt.isPresent()) {
			final String key = child.getPath().substring(baseLen);

			map.put(key, opt.get());
		}

		fillMap(map, child.getChildren(), baseLen);
	}
}
