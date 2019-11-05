package ascelion.config.core;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import ascelion.config.api.ConfigProvider;
import ascelion.config.spi.ConverterFactory;

import static ascelion.config.spi.Utils.pathElements;
import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ConfigRootBuilder implements ConfigProvider.Builder {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigProvider.Builder.class);

	private final Deque<ConfigNodeImpl> stack = new LinkedList<>();

	ConfigRootBuilder() {
		this.stack.push(new ConfigRootImpl());
	}

	ConfigRootBuilder(ConverterFactory converters) {
		this.stack.push(new ConfigRootImpl(converters));
	}

	@Override
	public ConfigRootBuilder child() {
		final ConfigNodeImpl node = this.stack.element();

		push(node.create("[" + node.children().size() + "]"));

		return this;
	}

	@Override
	public ConfigRootBuilder child(String path) {
		final String[] elements = pathElements(path);
		ConfigNodeImpl node = this.stack.element();

		for (final String element : elements) {
			node = node.create(element);
		}

		push(node);

		return this;
	}

	@Override
	public ConfigRootBuilder value(String value) {
		pull().value(value);

		return this;
	}

	@Override
	public ConfigRootBuilder back() {
		pull();

		return this;
	}

	@Override
	public ConfigRootBuilder set(Map<String, String> properties) {
		properties.forEach(this::set);

		return this;
	}

	@Override
	public ConfigRootBuilder set(String path, String value) {
		try {
			final String[] values = value.split("(?!\\\\),");

			if (values.length > 1) {
				child(path);

				for (final String v : values) {
					child().value(v);
				}

				back();
			} else if (value.length() > 0) {
				child(path).value(value);
			}
		} catch (final Throwable t) {
			LOG.error(format("At %s, setting %s to %s", this.stack.peek().path, path, value), t);

			reset();

			throw t;
		}

		return this;
	}

	@Override
	public ConfigRootImpl get() {
		reset();

		return (ConfigRootImpl) this.stack.pop();
	}

	private void reset() {
		while (this.stack.size() > 1) {
			pull();
		}
	}

	private void push(ConfigNodeImpl node) {
		LOG.trace("PUSH {}", node);

		this.stack.push(node);
	}

	private ConfigNodeImpl pull() {
		final ConfigNodeImpl node = this.stack.pop();

		LOG.trace("PULL {}", node);

		return node;
	}
}
