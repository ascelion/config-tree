package ascelion.config.core;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import ascelion.config.api.ConfigNode;
import ascelion.config.eval.Expression;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import lombok.Getter;

public class ConfigNodeImpl implements ascelion.config.api.ConfigNode {
	static final String VAR_PREFIX_PROP = "ascelion.config.var.prefix";
	static final String VAR_SEPARATOR_PROP = "ascelion.config.var.separator";
	static final String VAR_SUFFIX_PROP = "ascelion.config.var.suffix";

	final ConfigRootImpl root;
	@Getter
	final String name;
	@Getter
	final String path;

	private String value;
	private final Map<String, ConfigNodeImpl> children = new TreeMap<>();

	ConfigNodeImpl() {
		this.root = (ConfigRootImpl) this;
		this.name = "";
		this.path = "";
	}

	private ConfigNodeImpl(ConfigNodeImpl parent, String name) {
		if (name.isEmpty() || name.contains(".")) {
			throw new IllegalArgumentException(name);
		}

		this.root = parent.root;
		this.name = name;
		this.path = parent.path.isEmpty() ? name : parent.path + (isArrayName(name) ? name : "." + name);
	}

	@Override
	public String toString() {
		return format("[path: %s, value: %s, children: %d]", this.path, this.value, this.children.size());
	}

	@Override
	public final Optional<String> getValue() {
		return ofNullable(this.value)
				.map(this.root::eval);
	}

//	@Override
//	public final Optional<ConfigNode> getChild(String name) {
//		return ofNullable(children().get(name));
//	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public final Collection<ConfigNode> getChildren() {
		final Collection<ConfigNodeImpl> nodes = children().values();

		if (nodes.size() > 0) {
			return (Collection) nodes;
		}
		if (this.value == null) {
			return emptyList();
		}

		final Expression.Result eval = this.root.expression.eval(this.value);
		final String newPath;

		if (eval.getValue() != null) {
			newPath = eval.getValue();
		} else {
			newPath = eval.getLastVariable();
		}
		if (this.value.equals(newPath)) {
			return emptyList();
		}

		return this.root.getValue(newPath, ConfigNode.class)
				.map(ConfigNode::getChildren)
				.orElse(emptyList());
	}

	final ConfigNodeImpl child(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("The node name cannot be empty");
		}

		return children().get(name);
	}

	static private boolean isArrayName(String name) {
		return name.matches("^\\[\\d+\\]$");
	}

	final ConfigNodeImpl create(String name) {
		final boolean isArray = isArrayName(name);

		if (isArray && isMap()) {
			throw new IllegalStateException(format("The node %s already contains a map", this.path));
		}
		if (!isArray && isArray()) {
			throw new IllegalStateException(format("The node %s already contains an array", this.path));
		}

		return children().computeIfAbsent(name, n -> new ConfigNodeImpl(this, name));
	}

	Map<String, ConfigNodeImpl> children() {
		return this.children;
	}

	ConfigNodeImpl value(String value) {
		switch (this.path) {
			case VAR_PREFIX_PROP:
				this.root.expression.withPrefix(value);
			break;

			case VAR_SEPARATOR_PROP:
				this.root.expression.withValueSep(value);
			break;

			case VAR_SUFFIX_PROP:
				this.root.expression.withSuffix(value);
			break;
		}

		this.value = value;

		return this;
	}

	boolean isMap() {
		return this.children.size() > 0 && !isArrayName(this.children.values().iterator().next().name);
	}

	boolean isArray() {
		return this.children.size() > 0 && isArrayName(this.children.values().iterator().next().name);
	}
}
