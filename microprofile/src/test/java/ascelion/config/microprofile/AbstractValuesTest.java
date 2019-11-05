package ascelion.config.microprofile;

import static org.hamcrest.Matchers.is;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

public abstract class AbstractValuesTest {

	@TestTemplate
	@ExtendWith(MicroprofileProvider.class)
	public void direct(ConfigProviderResolver resolver) {
		setUp(resolver);

		checkEqual("values.booleanValue", Boolean.class, is(true));
		checkEqual("values.intValue", Integer.class, is(12));
		checkEqual("values.longValue", Long.class, is(123L));
		checkEqual("values.floatValue", Float.class, is(1234F));
		checkEqual("values.doubleValue", Double.class, is(12345D));
		checkEqual("values.value", String.class, is("text"));
		checkEqual("values.values", String[].class, is(new String[] { "text1", "text2" }));
	}

	@TestTemplate
	@ExtendWith(MicroprofileProvider.class)
	public void primitives(ConfigProviderResolver resolver) {
		setUp(resolver);

		checkEqual("values.booleanValue", boolean.class, is(true));
		checkEqual("values.intValue", int.class, is(12));
		checkEqual("values.longValue", long.class, is(123L));
		checkEqual("values.floatValue", float.class, is(1234F));
		checkEqual("values.doubleValue", double.class, is(12345D));
	}

	@TestTemplate
	@ExtendWith(MicroprofileProvider.class)
	public void expression1(ConfigProviderResolver resolver) {
		setUp(resolver);

		checkEqual("values1.booleanValue", boolean.class, is(true));
		checkEqual("values1.intValue", int.class, is(12));
		checkEqual("values1.longValue", long.class, is(123L));
		checkEqual("values1.floatValue", float.class, is(1234F));
		checkEqual("values1.doubleValue", double.class, is(12345D));
	}

	@TestTemplate
	@ExtendWith(MicroprofileProvider.class)
	public void expression2(ConfigProviderResolver resolver) {
		setUp(resolver);

		checkEqual("values2.booleanValue", boolean.class, is(true));
		checkEqual("values2.intValue", int.class, is(12));
		checkEqual("values2.longValue", long.class, is(123L));
		checkEqual("values2.floatValue", float.class, is(1234F));
		checkEqual("values2.doubleValue", double.class, is(12345D));
	}

	protected abstract void setUp(ConfigProviderResolver resolver);

	protected abstract <T> void checkEqual(String property, Class<T> type, Matcher<T> matcher);

}
