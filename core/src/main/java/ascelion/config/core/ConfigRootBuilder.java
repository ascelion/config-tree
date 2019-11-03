package ascelion.config.core;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import ascelion.config.api.ConfigProvider;
import ascelion.config.spi.ConverterFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConfigRootBuilder implements ConfigProvider.Builder {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigRootBuilder.class);

	private final Deque<ConfigNodeImpl> stack = new LinkedList<>();

	ConfigRootBuilder() {
		this.stack.push(new ConfigRootImpl());
	}

	ConfigRootBuilder(ConverterFactory converters) {
		this.stack.push(new ConfigRootImpl(converters));
	}

	@Override
	public ConfigRootBuilder child() {
		final ConfigNodeImpl last = this.stack.element();
		final String name = "[" + last.children().size() + "]";

		this.stack.push(last.create(name));

		return this;
	}

	@Override
	public ConfigRootBuilder child(String path) {
		ConfigNodeImpl last = this.stack.element();
		int start = 0;
		int end;

		do {
			end = path.indexOf('.', start);

			final String name = end < 0
					? path.substring(start)
					: path.substring(start, end);

			start = end + 1;

			last = last.create(name);
		} while (end >= 0);

		this.stack.push(last);

		return this;
	}

	@Override
	public ConfigRootBuilder value(String value) {
		this.stack.pop().value(value);

		return this;
	}

	@Override
	public ConfigRootBuilder back() {
		this.stack.pop();

		return this;
	}

	@Override
	public ConfigRootBuilder set(Map<String, String> properties) {
		properties.forEach(this::set);

		return this;
	}

	@Override
	public ConfigRootBuilder set(String path, String value) {
		final String[] values = value.split("(?!\\\\),");

		child(path);

		if (values.length > 1) {
			for (final String v : values) {
				child().value(v);
			}

			back();
		} else {
			value(value);
		}

		return this;
	}

	@Override
	public ConfigRootImpl get() {
		while (this.stack.size() > 1) {
			this.stack.pop();
		}

		return (ConfigRootImpl) this.stack.pop();
	}
}
