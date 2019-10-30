package ascelion.config.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ascelion.config.api.ConfigNode;

import static java.util.Optional.ofNullable;

import lombok.Getter;

class ConfigNodeImpl implements ascelion.config.api.ConfigNode {

	@Getter
	private final String name;
	@Getter
	private final String path;

	String value;

	final Map<String, ConfigNodeImpl> children = new HashMap<>();

	ConfigNodeImpl() {
		this.name = null;
		this.path = null;
	}

	ConfigNodeImpl(ConfigNodeImpl parent, String name) {
		this.name = name;
		this.path = parent.name != null ? parent.name + "." + name : name;

		parent.children.put(name, this);
	}

	@Override
	public Optional<String> getValue() {
		return ofNullable(null);
	}

	@Override
	public Optional<ConfigNode> child(String name) {
		return ofNullable(this.children.get(name));
	}

	@Override
	public Collection<? extends ConfigNode> children() {
		return this.children.values();
	}

}
