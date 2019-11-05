package ascelion.config.convert;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

final class MapConverter<M extends Map<String, T>, T> extends WrappedConverter<M, T> {
	private final Supplier<M> sup;

	MapConverter(Supplier<M> sup, Type type, ConfigConverter<T> conv) {
		super(type, conv);

		this.sup = sup;
	}

	@Override
	public Optional<M> convert(ConfigNode node) {
		final Collection<ConfigNode> children = node.getChildren();

		if (children.isEmpty()) {
			return Optional.empty();
		}

		final M map = this.sup.get();

		for (final ConfigNode child : children) {
			final Optional<T> opt = this.conv.convert(child);

			if (opt.isPresent()) {
				map.put(node.getPath(), opt.get());
			}
		}

		return Optional.of(map);
	}

}
