package ascelion.config.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import static java.lang.String.format;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class Utils {

	static private final Pattern ARRAY_INDEX = Pattern.compile("^\\[\\d+\\]$");

	static String[] pathElements(@NonNull String path) {
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

	static boolean isArrayName(String name) {
		return ARRAY_INDEX.matcher(name).matches();
	}

	private static void append(Collection<String> items, StringBuilder item, String path, int pos) {
		if (item.length() == 0) {
			throw new IllegalArgumentException(format("Invalid path format at position %d: %s", pos, path));
		}

		items.add(item.toString());
		item.delete(0, item.length());
	}

}
