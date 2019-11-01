package ascelion.config.core;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import ascelion.config.api.ConfigNode;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import lombok.Getter;

public class ConfigNodeImpl implements ascelion.config.api.ConfigNode {

	private final ConfigNodeImpl parent;
	@Getter
	private final String name;
	@Getter
	private final String path;

	private String value;
	private final Map<String, ConfigNodeImpl> children = new TreeMap<>();

	ConfigNodeImpl() {
		this.parent = null;
		this.name = "";
		this.path = "";
	}

	ConfigNodeImpl(ConfigNodeImpl parent, String name) {
		this.parent = parent;
		this.name = name;
		this.path = parent.path.isEmpty() ? name : parent.path + "." + name;
	}

	@Override
	public String toString() {
		return format("[path: %s, value: %s, children: %d]", this.path, this.value, this.children.size());
	}

	@Override
	public final Optional<String> getValue() {
		return ofNullable(this.value);
	}

	@Override
	public final Optional<ConfigNode> getChild(String name) {
		return ofNullable(children().get(name));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final Collection<ConfigNode> getChildren() {
		return (Collection) children().values();
	}

	final String value() {
		return this.value;
	}

	final ConfigNodeImpl child(String name, boolean create) {
		return create
				? children().computeIfAbsent(name, n -> new ConfigNodeImpl(this, n))
				: children().get(name);
	}

	Map<String, ConfigNodeImpl> children() {
		return this.children;
	}

	final ConfigNodeImpl parent() {
		return this.parent;
	}

	final ConfigNodeImpl value(String value) {
		this.value = value;

		return this;
	}

	final void reset() {
		this.value = null;
		this.children.clear();
	}
}
