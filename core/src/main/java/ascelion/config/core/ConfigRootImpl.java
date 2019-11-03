package ascelion.config.core;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRoot;
import ascelion.config.eval.Expression;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConverterFactory;

import static java.util.Optional.ofNullable;

import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
class ConfigRootImpl extends ConfigNodeImpl implements ConfigRoot {
	private final Collection<ConfigInput> inputs = new CopyOnWriteArrayList<>();

	enum State {
		DIRTY, LOADING, LOADED
	}

	private final AtomicReference<State> state = new AtomicReference<>(State.LOADED);
	private final ConverterFactory converters;

	final Expression expression = new Expression();

	ConfigRootImpl() {
		this(new ConverterFactory() {
			@Override
			public <T> ConfigConverter<T> get(Type type) {
				return node -> (Optional<T>) node.getValue();
			}
		});
	}

	public ConfigRootImpl(ConverterFactory converters) {
		this.converters = converters;

		this.expression.withLookup(x -> {
			final Optional<ConfigNodeImpl> node = findNode(x);

			if (node.isPresent()) {
				return new Expression.Lookup(node.get().getValue());
			} else {
				return new Expression.Lookup();
			}
		});
	}

	@Override
	public <T> Optional<T> getValue(String path, Type type) {
		final Optional<ConfigNodeImpl> find = findNode(path);

		if (type == ConfigNode.class) {
			return (Optional<T>) find;
		}

		return find.flatMap(n -> convert(n, type));
	}

	void addConfigInputs(Collection<ConfigInput> inputs) {
		try {
			this.inputs.addAll(inputs);
		} finally {
			this.state.set(State.DIRTY);
		}
	}

	@Override
	@SneakyThrows
	Map<String, ConfigNodeImpl> children() {
		if (this.state.compareAndSet(State.DIRTY, State.LOADING)) {
			readInputs();
		}

		while (this.state.get() == State.LOADING) {
			Thread.yield();
		}

		return super.children();
	}

	@SneakyThrows
	private void readInputs() {
		try {
			final ConfigRootBuilder bld = new ConfigRootBuilder();

			this.inputs.stream().sorted().forEach(i -> i.update(bld));

			super.children().clear();
			super.children().putAll(bld.get().children());

			this.state.set(State.LOADED);
		} catch (final Throwable t) {
			this.state.set(State.DIRTY);

			throw t;
		}
	}

	final String eval(String expression) {
		return this.expression.eval(expression).getValue();
	}

	final Optional<ConfigNodeImpl> findNode(String path) {
		ConfigNodeImpl node = this;
		int start = 0;
		int end;

		do {
			end = path.indexOf('.', start);

			final String name = end < 0
					? path.substring(start)
					: path.substring(start, end);

			start = end + 1;

			node = node.child(name);
		} while (node != null && end >= 0);

		return ofNullable(node);
	}

	@Override
	ConfigNodeImpl value(String value) {
		throw new UnsupportedOperationException();
	}

	private <T> T convert(ConfigNode node, Type type) {
		return (T) this.converters.get(type).convert(node);
	}
}
