package ascelion.config.convert;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

final class PrimitiveArrayConverter<A, T> extends WrappedConverter<A, T> {

	PrimitiveArrayConverter(Type type, ConfigConverter<T> conv) {
		super(type, conv);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<A> convert(ConfigNode node) {
		final Collection<ConfigNode> children = node.getChildren();

		if (children.isEmpty() && !node.getValue().isPresent()) {
			return Optional.empty();
		}

		final Stream<Optional<T>> stream;

		if (children.isEmpty()) {
			stream = Stream.of(this.conv.convert(node));
		} else {
			stream = children.stream().map(this.conv::convert);
		}

		if (this.type == int.class) {
			return Optional.of((A) stream
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(Integer.class::cast)
					.mapToInt(Number::intValue)
					.toArray());
		}
		if (this.type == long.class) {
			return Optional.of((A) stream
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(Long.class::cast)
					.mapToLong(Number::longValue)
					.toArray());
		}
		if (this.type == Double.class) {
			return Optional.of((A) stream
					.filter(Optional::isPresent)
					.map(Optional::get)
					.map(Double.class::cast)
					.mapToDouble(Number::doubleValue)
					.toArray());
		}

		throw new UnsupportedOperationException();
	}
}
