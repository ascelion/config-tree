package ascelion.config.microprofile;

import static org.hamcrest.MatcherAssert.assertThat;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.hamcrest.Matcher;

public class ConfigTreeValuesTest extends AbstractValuesTest {

	private ConfigRoot root;

	@Override
	protected void setUp(ConfigProviderResolver resolver) {
		ConfigProviderResolver.setInstance(resolver);

		this.root = ConfigProvider.root();
	}

	@Override
	protected <T> void checkEqual(String property, Class<T> type, Matcher<T> matcher) {
		assertThat(this.root.getValue("ascelion." + property, type).get(), matcher);
	}
}
