package ascelion.config.convert;

import static ascelion.config.spi.Utils.isArrayNode;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class CollectionConverter<C extends Collection<T>, T> extends WrappedConverter<C, T> {
	private final Supplier<C> sup;

	CollectionConverter(Supplier<C> sup, Type type, ConfigConverter<T> conv) {
		super(type, conv);

		this.sup = sup;
	}

	@Override
	public Optional<C> convert(ConfigNode node) {
		final Collection<ConfigNode> children = node.getChildren();
		final Stream<Optional<T>> stream;

		if (isArrayNode(node.getChildren())) {
			stream = children.stream().map(this.conv::convert);
		} else if (node.getValue().isPresent()) {
			stream = Stream.of(this.conv.convert(node));
		} else {
			stream = Stream.empty();
		}

		final C col = this.sup.get();

		stream.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(col::add);

		return Optional.of(col);
	}

}
