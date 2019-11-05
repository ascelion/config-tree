package ascelion.config.convert;

import java.util.Optional;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.spi.ConfigInputReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class StringConvertTest {
	{
		System.setProperty(ConfigInputReader.RESOURCE_PROP, getClass().getSimpleName());
	}

	@Test
	@Disabled
	public void run() {
		final ConfigRoot root = ConfigProvider.root();

		final Optional<String> v1 = root.getValue("prop1.values", String.class);
		final Optional<String[]> a1 = root.getValue("prop1.values", String[].class);

		final Optional<String> v2 = root.getValue("prop2.values", String.class);
		final Optional<String[]> a2 = root.getValue("prop2.values", String[].class);

		assertThat(v1.isPresent(), is(true));
		assertThat(a1.isPresent(), is(true));

		assertThat(v2.isPresent(), is(true));
		assertThat(a2.isPresent(), is(true));
	}
}
