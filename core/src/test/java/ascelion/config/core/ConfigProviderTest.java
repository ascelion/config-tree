package ascelion.config.core;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.spi.ConfigInputReader;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigProviderTest {

	@BeforeEach
	public void setUp() {
		System.setProperty(ConfigInputReader.RESOURCE_PROP, getClass().getSimpleName());
	}

	@Test
	public void load() {
		ConfigProvider.load();
	}

	@Test
	public void string_read() {
		final ConfigRoot root = ConfigProvider.load().get();

		final String sv1 = root.getValue("value", String.class).orElse(null);
		final String[] sv2 = root.getValue("value", String[].class).orElse(null);
		final String sv3 = root.getValue("values", String.class).orElse(null);
		final String[] sv4 = root.getValue("values", String[].class).orElse(null);

		assertThat(sv1, is(notNullValue()));
		assertThat(sv2, is(notNullValue()));
		assertThat(sv3, is(nullValue()));
		assertThat(sv4, is(notNullValue()));

		assertThat(sv1, equalTo("1"));
		assertThat(asList(sv2), contains("1"));
		assertThat(asList(sv4), contains("2", "3"));
	}

	@Test
	public void integer_read() {
		final ConfigRoot root = ConfigProvider.load().get();

		final Integer sv1 = root.getValue("value", Integer.class).orElse(null);
		final Integer[] sv2 = root.getValue("value", Integer[].class).orElse(null);
		final Integer sv3 = root.getValue("values", Integer.class).orElse(null);
		final Integer[] sv4 = root.getValue("values", Integer[].class).orElse(null);

		assertThat(sv1, is(notNullValue()));
		assertThat(sv2, is(notNullValue()));
		assertThat(sv3, is(nullValue()));
		assertThat(sv4, is(notNullValue()));

		assertThat(sv1, equalTo(1));
		assertThat(asList(sv2), contains(1));
		assertThat(asList(sv4), contains(2, 3));
	}

	@Test
	public void int_read() {
		final ConfigRoot root = ConfigProvider.load().get();

		final int sv1 = root.getValue("value", int.class).orElse(0);
		final int[] sv2 = root.getValue("value", int[].class).orElse(null);
		final int sv3 = root.getValue("values", int.class).orElse(0);
		final int[] sv4 = root.getValue("values", int[].class).orElse(null);

		assertThat(sv1, is(notNullValue()));
		assertThat(sv2, is(notNullValue()));
		assertThat(sv3, is(0));
		assertThat(sv4, is(notNullValue()));

		assertThat(sv1, equalTo(1));
		assertThat(sv2, equalTo(new int[] { 1 }));
		assertThat(sv4, equalTo(new int[] { 2, 3 }));
	}
}
