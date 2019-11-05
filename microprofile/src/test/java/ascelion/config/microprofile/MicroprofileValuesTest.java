package ascelion.config.microprofile;

import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.hamcrest.Matcher;

public class MicroprofileValuesTest extends AbstractValuesTest {
	private Config config;

	@Override
	protected void setUp(ConfigProviderResolver resolver) {
		ConfigProviderResolver.setInstance(resolver);

		this.config = resolver.getConfig();
	}

	@Override
	protected <T> void checkEqual(String property, Class<T> type, Matcher<T> matcher) {
		assertThat(this.config.getValue("ascelion." + property, type), matcher);
	}

}
