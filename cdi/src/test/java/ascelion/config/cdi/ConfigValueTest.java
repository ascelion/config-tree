package ascelion.config.cdi;

import javax.inject.Inject;

import ascelion.config.api.ConfigPrefix;
import ascelion.config.api.ConfigValue;

import static ascelion.config.cdi.WeldRule.createWeldRule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@EnableWeld
@Disabled
public class ConfigValueTest {

	@ConfigPrefix("values")
	static class Values1 {
		@ConfigValue
		boolean booleanValue;
		@ConfigValue
		int intValue;
		@ConfigValue
		long longValue;
		@ConfigValue
		float floatValue;
		@ConfigValue
		double doubleValue;
		@ConfigValue
		String value;
	}

	static class Values2 {
		@ConfigValue("${values.booleanValue}")
		boolean booleanValue;
		@ConfigValue("values.intValue")
		int intValue;
		@ConfigValue("values.longValue")
		long longValue;
		@ConfigValue("values.floatValue")
		float floatValue;
		@ConfigValue("values.doubleValue")
		double doubleValue;
		@ConfigValue("values.value")
		String value;
	}

	@WeldSetup
	public WeldInitiator weld = createWeldRule(this, Values1.class, Values2.class);

	@Inject
	private Values1 values1;
	@Inject
	private Values1 values2;

	@Test
	public void withPrefix() {
		assertThat(this.values1.booleanValue, equalTo(true));
		assertThat(this.values1.intValue, equalTo(12));
		assertThat(this.values1.longValue, equalTo(123L));
		assertThat(this.values1.floatValue, equalTo(1234F));
		assertThat(this.values1.doubleValue, equalTo(12345D));
	}

	@Test
	public void withoutPrefix() {
		assertThat(this.values2.booleanValue, equalTo(true));
		assertThat(this.values2.intValue, equalTo(12));
		assertThat(this.values2.longValue, equalTo(123L));
		assertThat(this.values2.floatValue, equalTo(1234F));
		assertThat(this.values2.doubleValue, equalTo(12345D));
	}
}