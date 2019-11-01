package ascelion.config.convert;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

public class CollectionConverter<C extends Collection<T>, T> extends WrappedConverter<C, T> {
	private final Supplier<C> sup;

	CollectionConverter(Supplier<C> sup, Type type, ConfigConverter<T> conv) {
		super(type, conv);

		this.sup = sup;
	}

	@Override
	public Optional<C> convert(ConfigNode node) {
		final Collection<ConfigNode> children = node.getChildren();

		if (children.isEmpty() && !node.getValue().isPresent()) {
			return Optional.empty();
		}

		Stream<Optional<T>> stream;

		if (children.isEmpty()) {
			stream = Stream.of(this.conv.convert(node));
		} else {
			stream = children.stream().map(this.conv::convert);
		}

		final C col = this.sup.get();

		stream.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(col::add);

		return Optional.of(col);
	}

}
