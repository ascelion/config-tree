package ascelion.config.core;

import java.io.IOException;
import java.util.Optional;

import ascelion.config.read.PropertiesInputReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class ConfigRootTest {

	private final ConfigRootImpl root = new ConfigRootImpl();

	@Test
	public void paths() {
		//@formatter:off
		this.root
			.child("1", true).value("1")
				.child("1", true).value( "11")
			.parent()
				.child("2", true).value("12")
			.parent()
		.parent()
			.child("2",true).value( "2")
				.child("1",true).value( "21")
			.parent()
				.child("2", true).value("22")
		;
		//@formatter:on

		checkValue(this.root, "1", "1");
		checkValue(this.root, "1.1", "11");
		checkValue(this.root, "1.2", "12");

		checkValue(this.root, "2", "2");
		checkValue(this.root, "2.1", "21");
		checkValue(this.root, "2.2", "22");
	}

	@Test
	public void build() throws IOException {
		final PropertiesInputReader reader = new PropertiesInputReader();

		this.root.addConfigInputs(reader.read(getClass().getSimpleName()));

		checkValue(this.root, "1", "1");
		checkValue(this.root, "1.1", "11");
		checkValue(this.root, "1.2", "12");

		checkValue(this.root, "2", "2");
		checkValue(this.root, "2.1", "21");
		checkValue(this.root, "2.2", "22");
	}

	@Test
	public void priority() throws IOException {
		final PropertiesInputReader reader = new PropertiesInputReader();

		this.root.addConfigInputs(reader.read(getClass().getSimpleName()));
		this.root.addConfigInputs(reader.read(getClass().getSimpleName() + "_X"));

		checkValue(this.root, "1", "X1");
		checkValue(this.root, "1.1", "X11");
		checkValue(this.root, "1.2", "X12");

		checkValue(this.root, "2", "X2");
		checkValue(this.root, "2.1", "X21");
		checkValue(this.root, "2.2", "X22");
	}

	private void checkValue(ConfigRootImpl root, String path, String expected) {
		final Optional<String> value = root.eval(path, String.class);

		assertThat(path, value.isPresent(), equalTo(expected != null));

		if (expected != null) {
			assertThat(value.get(), equalTo(expected));
		}
	}
}