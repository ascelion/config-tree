package ascelion.config.convert;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.core.AbstractTest;
import ascelion.config.spi.ConfigInputReader;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.leangen.geantyref.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntConvertTest extends AbstractTest {
	static private final Type STRING_COL = TypeFactory.parameterizedClass(Collection.class, Integer.class);

	private ConfigRoot root;

	@BeforeEach
	public void setUp() {

		System.setProperty(ConfigInputReader.RESOURCE_PROP, getClass().getSimpleName());

		this.root = ConfigProvider.root();
	}

	@Test
	public void fromPropertiesWithComma() {
		final Optional<Integer> v = this.root.getValue("prop1.values1", Integer.class);
		final Optional<Integer[]> a = this.root.getValue("prop1.values1", Integer[].class);
		final Optional<List<Integer>> c = this.root.getValue("prop1.values1", STRING_COL);

		assertThat(v.isPresent(), is(false));
		assertThat(a.isPresent(), is(true));
		assertThat(c.isPresent(), is(true));

		assertThat(asList(a.get()), equalTo(asList(111, 112)));
		assertThat(c.get(), equalTo(asList(111, 112)));
	}

	@Test
	public void fromProperties() {
		final Optional<Integer> v = this.root.getValue("prop1.values2", Integer.class);
		final Optional<Integer[]> a = this.root.getValue("prop1.values2", Integer[].class);
		final Optional<List<Integer>> c = this.root.getValue("prop1.values2", STRING_COL);

		assertThat(v.isPresent(), is(true));
		assertThat(a.isPresent(), is(true));
		assertThat(c.isPresent(), is(true));

		assertThat(v.get(), equalTo(12));
		assertThat(asList(a.get()), equalTo(asList(12)));
		assertThat(c.get(), equalTo(asList(12)));
	}

	@Test
	public void fromYamlWithArray() {
		final Optional<Integer> v = this.root.getValue("prop2.values2", Integer.class);
		final Optional<Integer[]> a = this.root.getValue("prop2.values2", Integer[].class);
		final Optional<List<Integer>> c = this.root.getValue("prop2.values2", STRING_COL);

		assertThat(v.isPresent(), is(false));
		assertThat(a.isPresent(), is(true));
		assertThat(c.isPresent(), is(true));

		assertThat(asList(a.get()), equalTo(asList(221, 222)));
		assertThat(c.get(), equalTo(asList(221, 222)));
	}
}
