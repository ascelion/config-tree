package ascelion.config.core;

import static ascelion.config.core.Utils.pathElements;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class UtilsTest {

	@Test
	public void path_elements() {
		assertThat(asList(pathElements("a.b.c")), equalTo(asList("a", "b", "c")));
		assertThat(asList(pathElements("a.b[3].c")), equalTo(asList("a", "b", "[3]", "c")));

		assertThrows(NullPointerException.class, () -> pathElements(null));
		assertThrows(IllegalArgumentException.class, () -> pathElements(""));
		assertThrows(IllegalArgumentException.class, () -> pathElements("a.b["));
		assertThrows(IllegalArgumentException.class, () -> pathElements("a.b]"));
		assertThrows(IllegalArgumentException.class, () -> pathElements(".a.b"));
		assertThrows(IllegalArgumentException.class, () -> pathElements("a[0]b"));
		assertThrows(IllegalArgumentException.class, () -> pathElements("a..b"));
		assertThrows(IllegalArgumentException.class, () -> pathElements("a.[0].b"));
		assertThrows(IllegalArgumentException.class, () -> pathElements("[]"));
		assertThrows(IllegalArgumentException.class, () -> pathElements("a[].c"));
		assertThrows(IllegalArgumentException.class, () -> pathElements("[x]"));
	}
}
