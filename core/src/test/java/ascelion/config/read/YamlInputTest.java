package ascelion.config.read;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.spi.ConfigInputReader;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class YamlInputTest {

	@Test
	public void run() {
		System.setProperty(ConfigInputReader.RESOURCE_PROP, getClass().getSimpleName());

		final ConfigRoot root = ConfigProvider.load().get();

		assertThat(root.getValues("prop1"), equalTo(asList("value11", "value12")));
		assertThat(root.getValue("prop2"), equalTo("value2"));

//		assertThat(m1, hasEntry("prop2.prop21.0", "value211"));
//		assertThat(m1, hasEntry("prop2.prop21.1", "value212"));
//		assertThat(m1, hasEntry("prop2.prop22.prop221", "ok"));
	}

}
