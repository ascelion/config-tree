package ascelion.config.convert;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

import ascelion.config.core.AbstractTest;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class MapConvertTest extends AbstractTest {
	static private final Type STRING_MAP = parameterizedClass(Map.class, String.class, String.class);

	public MapConvertTest() {
		super("interfaces");
	}

	@Test
	public void withMap() {
		final Optional<Map<String, String>> v = this.root.getValue("databases.db1", STRING_MAP);

		assertThat(v.isPresent(), is(true));
		assertThat(v.get(), hasKey("type"));
		assertThat(v.get(), hasKey("properties.prop1"));
	}

	@Test
	public void withProperties() {
		final Optional<Properties> v = this.root.getValue("databases.db1", Properties.class);

		assertThat(v.isPresent(), is(true));
		assertThat(v.get(), hasKey("type"));
		assertThat(v.get(), hasKey("properties.prop1"));
	}
}
