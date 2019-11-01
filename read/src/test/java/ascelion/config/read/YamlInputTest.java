package ascelion.config.read;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ascelion.config.spi.ConfigInput;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.Test;

public class YamlInputTest {

	@Test
	public void run() throws IOException {
		final YamlInputReader reader = new YamlInputReader();
		final List<ConfigInput> inputs = reader.read(getClass().getSimpleName())
				.stream().sorted().collect(toList());

		assertThat(inputs, hasSize(2));

		final Map<String, String> m0 = inputs.get(0).properties();
		final Map<String, String> m1 = inputs.get(1).properties();

		assertThat(m0, hasEntry("prop1.0", "value11"));
		assertThat(m0, hasEntry("prop1.1", "value12"));
		assertThat(m0, hasEntry("prop2", "value2"));

		assertThat(m1, hasEntry("prop2.prop21.0", "value211"));
		assertThat(m1, hasEntry("prop2.prop21.1", "value212"));
		assertThat(m1, hasEntry("prop2.prop22.prop221", "ok"));
	}

}
