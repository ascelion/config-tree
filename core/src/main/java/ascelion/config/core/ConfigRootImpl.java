package ascelion.config.core;

import static java.lang.String.format;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRoot;
import ascelion.config.eval.Expression;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConfigInput;
import ascelion.config.spi.ConverterFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
final class ConfigRootImpl extends ConfigNodeImpl implements ConfigRoot {
	private final Set<ConfigInput> inputs = new CopyOnWriteArraySet<>();

	enum State {
		DIRTY, LOADING, LOADED
	}

	private final AtomicReference<State> state = new AtomicReference<>(State.LOADED);
	private final ConverterFactory converters;

	ConfigRootImpl() {
		this(new ConverterFactory() {
			@Override
			public <T> ConfigConverter<T> get(Type type) {
				return (node) -> (Optional<T>) node.getValue();
			}
		});
	}

	public ConfigRootImpl(ConverterFactory converters) {
		this.converters = converters;
	}

	@Override
	public <T> Optional<T> getValue(String path, Type type) {
		final Optional<ConfigNodeImpl> node = findNode(path);

		if (type == ConfigNode.class) {
			return (Optional<T>) node;
		}
		if (node.isPresent()) {
			return convert(node.get(), type);
		}

		final Expression.Result eval = eval(path);

		if (eval.isEmpty()) {
			return Optional.empty();
		}
		if (eval.isResolved()) {
			final ConfigRootImpl temp = new ConfigRootBuilder(this.converters)
					.set(eval.getLastVariable(), eval.getValue())
					.get();

			return temp.getValue(eval.getLastVariable(), type);
		}

		return Optional.empty();
	}

	void addConfigInputs(Collection<ConfigInput> inputs) {
		try {
			this.inputs.addAll(inputs);
		} finally {
			this.state.set(State.DIRTY);
		}
	}

	@Override
	Map<String, ConfigNodeImpl> children() {
		if (this.state.compareAndSet(State.DIRTY, State.LOADING)) {
			readInputs();
		}

		while (this.state.get() == State.LOADING) {
			Thread.yield();
		}

		return super.children();
	}

	@Override
	ConfigNodeImpl value(String value) {
		throw new UnsupportedOperationException();
	}

	private void readInputs() {
		try {
			final ConfigRootBuilder bld = new ConfigRootBuilder();

			this.inputs.stream()
					.sorted()
					.forEach(i -> update(bld, i));

			merge(bld.get(), true);

			this.state.set(State.LOADED);
		} catch (final Throwable t) {
			this.state.set(State.DIRTY);

			throw t;
		}
	}

	private void update(ConfigRootBuilder bld, ConfigInput inp) {
		try {
			inp.update(bld);
		} catch (final Exception e) {
			log.error(format("Error reading %s", inp.name()), e);
		}
	}

	private <T> T convert(ConfigNode node, Type type) {
		return (T) this.converters.get(type).convert(node);
	}
}
