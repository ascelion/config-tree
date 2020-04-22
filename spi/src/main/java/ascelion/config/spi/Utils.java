package ascelion.config.spi;

import static java.lang.String.format;

import ascelion.config.api.ConfigNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

	static private final Pattern ARRAY_INDEX = Pattern.compile("^\\[\\d+\\]$");

	static public String[] pathElements(@NonNull String path) {
		final int size = path.length();

		if (size == 0) {
			throw new IllegalArgumentException("Empty path is not allowed");
		}

		final Collection<String> elements = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		boolean index = false;
		int pos = 0;

		for (; pos < size; pos++) {
			final char c = path.charAt(pos);

			switch (c) {
				case '.':
					if (pos == 0 || pos == size - 1 || index) {
						throw new IllegalArgumentException(format("Invalid path format at position %d: %s", pos, path));
					}

					append(elements, sb, path, pos);
				break;

				case '[':
					if (index) {
						throw new IllegalArgumentException(format("Invalid path format at position %d: %s", pos, path));
					}

					index = true;

					append(elements, sb, path, pos);
					sb.append(c);
				break;

				case ']':
					if (!index || sb.length() == 1 || (pos < size - 1 && path.charAt(pos + 1) != '.')) {
						throw new IllegalArgumentException(format("Invalid path format at position %d: %s", pos, path));
					}

					index = false;

					sb.append(c);
				break;

				default:
					if (index && !Character.isDigit(c)) {
						throw new IllegalArgumentException(format("Invalid path format at position %d: %s", pos, path));
					}

					sb.append(c);
			}
		}

		if (index) {
			throw new IllegalArgumentException(format("Invalid path format at position %d: %s", pos, path));
		}

		if (sb.length() > 0) {
			append(elements, sb, path, pos);
		}

		return elements.toArray(new String[elements.size()]);
	}

	static public boolean isSimpleArray(ConfigNode node) {
		if (node.getValue().isPresent()) {
			return false;
		}

		final Collection<ConfigNode> children = node.getChildren();

		if (children.isEmpty()) {
			return false;
		}
		if (!isArrayName(children.iterator().next().getName())) {
			return false;
		}

		final long gchildren = children.stream()
				.flatMap(child -> child.getChildren().stream())
				.count();

		return gchildren == 0;
	}

	static public boolean isSimpleMap(ConfigNode node) {
		if (node.getValue().isPresent()) {
			return false;
		}

		final Collection<ConfigNode> children = node.getChildren();

		if (children.isEmpty()) {
			return false;
		}
		if (isArrayName(children.iterator().next().getName())) {
			return false;
		}

		final long gchildren = children.stream()
				.flatMap(child -> child.getChildren().stream())
				.count();

		return gchildren == 0;
	}

	static public boolean isMapNode(Collection<? extends ConfigNode> nodes) {
		return nodes.size() > 0 && !isArrayName(nodes.iterator().next().getName());
	}

	static public boolean isArrayNode(Collection<? extends ConfigNode> nodes) {
		return nodes.size() > 0 && isArrayName(nodes.iterator().next().getName());
	}

	static public boolean isArrayName(String name) {
		return ARRAY_INDEX.matcher(name).matches();
	}

	static private void append(Collection<String> items, StringBuilder item, String path, int pos) {
		if (item.length() == 0) {
			throw new IllegalArgumentException(format("Invalid path format at position %d: %s", pos, path));
		}

		items.add(item.toString());
		item.delete(0, item.length());
	}

}
