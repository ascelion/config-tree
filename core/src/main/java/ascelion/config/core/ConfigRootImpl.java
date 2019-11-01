package ascelion.config.core;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRoot;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConverterFactory;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
class ConfigRootImpl extends ConfigNodeImpl implements ConfigRoot {
	private final Collection<ConfigInput> inputs = new CopyOnWriteArrayList<>();

	enum State {
		DIRTY, LOADING, LOADED
	}

	private final AtomicReference<State> state = new AtomicReference<>(State.LOADED);
	private final ConverterFactory converters;

	ConfigRootImpl() {
		this(new ConverterFactory() {
			@Override
			public <T> ConfigConverter<T> get(Type type) {
				return node -> (Optional<T>) node.getValue();
			}
		});
	}

	@Override
	public <T> Optional<T> eval(String expression, Class<T> type) {
		ConfigNodeImpl node = this;
		int start = 0;
		int end;

		do {
			end = expression.indexOf('.', start);

			final String name = end < 0
					? expression.substring(start)
					: expression.substring(start, end);

			start = end + 1;

			node = node.child(name, false);
		} while (node != null && end >= 0);

		if (type == ConfigNode.class) {
			return (Optional<T>) ofNullable(node);
		} else if (node != null) {
			return convert(node, type);
		} else {
			return Optional.empty();
		}
	}

	ConfigNodeImpl set(String path, String value) {
		ConfigNodeImpl node = this;
		int start = 0;
		int end;

		do {
			end = path.indexOf('.', start);

			final String name = end < 0
					? path.substring(start)
					: path.substring(start, end);

			start = end + 1;

			node = node.child(name, true);
		} while (end >= 0);

		return node.value(value);
	}

	void addConfigInputs(Collection<ConfigInput> inputs) {
		try {
			this.inputs.addAll(inputs);
		} finally {
			this.state.set(State.DIRTY);

			reset();
		}
	}

	@Override
	Map<String, ConfigNodeImpl> children() {
		if (this.state.compareAndSet(State.DIRTY, State.LOADING)) {
			final List<ConfigInput> sorted = this.inputs.stream().sorted().collect(toList());

			sorted.forEach(this::add);

			this.state.set(State.LOADED);
		}

		return super.children();
	}

	private void add(ConfigInput input) {
		final Map<String, String> properties = input.properties();

		for (final Map.Entry<String, String> ent : properties.entrySet()) {
			set(ent.getKey(), ent.getValue());
		}
	}

	private <T> T convert(ConfigNode node, Type type) {
		return (T) this.converters.get(type).convert(node);
	}

}
