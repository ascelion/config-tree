package ascelion.config.microprofile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class ExpressionConfigSource implements ConfigSource {
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
			// check to see if it's a simple array
			final Collection<ConfigNode> children = node.getChildren();

			if (children.size() > 1) {
				final long gchildren = children.stream()
						.flatMap(child -> child.getChildren().stream())
						.count();

				if (gchildren == 0) {
					final String value = children.stream()
							.map(ConfigNode::getValue)
							.filter(Optional::isPresent)
							.map(Optional::get)
							.map(s -> s.replace(",", "\\,"))
							.collect(joining(","));

					properties.put(node.getPath(), value);

					return;
				}
			}

			children.forEach(child -> fillMap(properties, child));
		}
	}

}
