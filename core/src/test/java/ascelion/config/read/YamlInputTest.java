package ascelion.config.read;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;
import ascelion.config.core.AbstractTest;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class YamlInputTest extends AbstractTest {

	@Test
	public void run() {
		final ConfigRoot root = ConfigProvider.root();

		assertThat(root.getValues("prop1"), equalTo(asList("value11", "value12")));
		assertThat(root.getValue("prop2"), equalTo("value2"));

		assertThat(root.getValues("prop2.prop21"), equalTo(asList("value211", "value212")));
		assertThat(root.getValue("prop2.prop22.prop221"), equalTo("ok"));

		assertThat(root.getValue("prop2.prop21[0]"), equalTo("value211"));
		assertThat(root.getValue("prop2.prop21[1]"), equalTo("value212"));
	}

}
