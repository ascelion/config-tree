package ascelion.config.microprofile;

import static ascelion.config.spi.Utils.isSimpleArray;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.spi.ConfigSource;

public final class ExpressionConfigSource implements ConfigSource {
	static private final Mediator MEDIATOR = new Mediator();

	@Override
	public int getOrdinal() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Map<String, String> getProperties() {
		if (MEDIATOR.acquire()) {
			try {
				final Supplier<ConfigRoot> prov = () -> ConfigProvider.root();
				final ConfigRoot root = new InstanceProvider<>(ConfigRoot.class, prov).get();
				final Map<String, String> properties = new HashMap<>();

				fillMap(properties, root);

				return properties;
			} finally {
				MEDIATOR.release();
			}
		} else {
			return emptyMap();
		}
	}

	@Override
	public String getValue(String propertyName) {
		return getProperties().get(propertyName);
	}

	@Override
	public String getName() {
		return "config-tree";
	}

	private void fillMap(Map<String, String> properties, ConfigNode node) {
		if (node.getValue().isPresent()) {
			properties.put(node.getPath(), node.getValue().get());
		} else {
			final Collection<ConfigNode> children = node.getChildren();

			if (isSimpleArray(node)) {
				final String value = children.stream()
						.map(ConfigNode::getValue)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.map(s -> s.replace(",", "\\,"))
						.collect(joining(","));

				properties.put(node.getPath(), value);
			}

			children.forEach(child -> fillMap(properties, child));
		}
	}

}
