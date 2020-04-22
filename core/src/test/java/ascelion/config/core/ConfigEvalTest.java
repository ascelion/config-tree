package ascelion.config.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import ascelion.config.api.ConfigRoot;
import ascelion.config.convert.Converters;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ConfigEvalTest {

	@Test
	public void undefined() {
		final ConfigRootImpl root = new ConfigRootBuilder(new Converters()).get();
		final Optional<Boolean> value = root.getValue("undefined", boolean.class);

		assertThat(value.isPresent(), is(false));
	}

	@Test
	public void undefinedWithDefault() {
		final ConfigRootImpl root = new ConfigRootBuilder(new Converters()).get();
		final Optional<Boolean> value = root.getValue("${undefined:-true}", boolean.class);

		assertThat(value.isPresent(), is(true));
		assertThat(value.get(), is(true));
	}

	@Test
	public void undefinedPath() {
		final ConfigRootImpl root = new ConfigRootBuilder(new Converters()).get();
		final Optional<Boolean> value = root.getValue("un.defined", boolean.class);

		assertThat(value.isPresent(), is(false));
	}

	@Test
	public void undefinedPathWithDefault() {
		final ConfigRootImpl root = new ConfigRootBuilder(new Converters()).get();
		final Optional<Boolean> value = root.getValue("${un.defined:-true}", boolean.class);

		assertThat(value.isPresent(), is(true));
		assertThat(value.get(), is(true));
	}

	@Test
	public void boolean_ref() {
		final ConfigRootBuilder bld = new ConfigRootBuilder(new Converters());
		final ConfigRoot root = bld
				.set("prefix.value", "true")
				.set("value", "${prefix.value}")
				.get();

		final boolean i1 = root.getValue("prefix.value", Boolean.class).get();
		final boolean i2 = root.getValue("prefix.value", boolean.class).get();
		final boolean i3 = root.getValue("value", Boolean.class).get();
		final boolean i4 = root.getValue("value", boolean.class).get();

		assertThat(i1, is(true));
		assertThat(i2, is(true));
		assertThat(i3, is(true));
		assertThat(i4, is(true));
	}

	@Test
	public void int_ref() {
		final ConfigRootBuilder bld = new ConfigRootBuilder(new Converters());
		final ConfigRoot root = bld
				.set("prefix.value", "314")
				.set("value", "${prefix.value}")
				.get();

		final int i1 = root.getValue("prefix.value", Integer.class).get();
		final int i2 = root.getValue("prefix.value", int.class).get();
		final int i3 = root.getValue("value", Integer.class).get();
		final int i4 = root.getValue("value", int.class).get();

		assertThat(i1, is(314));
		assertThat(i2, is(314));
		assertThat(i3, is(314));
		assertThat(i4, is(314));
	}

	@Test
	public void long_ref() {
		final ConfigRootBuilder bld = new ConfigRootBuilder(new Converters());
		final ConfigRoot root = bld
				.set("prefix.value", "314")
				.set("value", "${prefix.value}")
				.get();

		final long i1 = root.getValue("prefix.value", Long.class).get();
		final long i2 = root.getValue("prefix.value", long.class).get();
		final long i3 = root.getValue("value", Long.class).get();
		final long i4 = root.getValue("value", long.class).get();

		assertThat(i1, is(314L));
		assertThat(i2, is(314L));
		assertThat(i3, is(314L));
		assertThat(i4, is(314L));
	}

	@Test
	public void double_ref() {
		final ConfigRootBuilder bld = new ConfigRootBuilder(new Converters());
		final ConfigRoot root = bld
				.set("prefix.value", "3.14159")
				.set("value", "${prefix.value}")
				.get();

		final double i1 = root.getValue("prefix.value", Double.class).get();
		final double i2 = root.getValue("prefix.value", double.class).get();
		final double i3 = root.getValue("value", Double.class).get();
		final double i4 = root.getValue("value", double.class).get();

		assertThat(i1, is(3.14159));
		assertThat(i2, is(3.14159));
		assertThat(i3, is(3.14159));
		assertThat(i4, is(3.14159));
	}
}
