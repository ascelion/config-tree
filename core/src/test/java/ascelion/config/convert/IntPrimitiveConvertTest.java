package ascelion.config.convert;

import java.util.Optional;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.core.AbstractTest;
import ascelion.config.spi.ConfigInputReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntPrimitiveConvertTest extends AbstractTest {
	private ConfigRoot root;

	@BeforeEach
	public void setUp() {
		System.setProperty(ConfigInputReader.RESOURCE_PROP, IntConvertTest.class.getSimpleName());

		this.root = ConfigProvider.root();
	}

	@Test
	public void fromPropertiesWithComma() {
		final Optional<int[]> a = this.root.getValue("prop1.values1", int[].class);

		assertThat(a.isPresent(), is(true));
		assertThat(a.get(), equalTo(new int[] { 111, 112 }));
	}

	@Test
	public void fromProperties() {
		final Optional<int[]> a = this.root.getValue("prop1.values2", int[].class);

		assertThat(a.isPresent(), is(true));
		assertThat(a.get(), equalTo(new int[] { 12 }));
	}

	@Test
	public void fromYamlWithArray() {
		final Optional<int[]> a = this.root.getValue("prop2.values2", int[].class);

		assertThat(a.isPresent(), is(true));

		assertThat(a.get(), equalTo(new int[] { 221, 222 }));
	}
}
