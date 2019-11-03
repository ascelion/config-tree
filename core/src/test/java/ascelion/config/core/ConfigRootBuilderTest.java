package ascelion.config.core;

import java.util.Optional;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class ConfigRootBuilderTest {

	private final ConfigProvider.Builder bld = new ConfigRootBuilder();

	@Test
	void simpleBuild() {
		final ConfigRoot root = this.bld
		//@formatter:off
			.child("x.1")
				.child("1")
					.child("1").value("x.1.1.1")
					.back()
				.child("2").value("x.1.2")
				.back()
			.child("x.2")
				.child("1").value("x.2.1")
				.child("2").value("x.2.2")
			.get();
		//@formatter:on

		check(root);
	}

	private void check(ConfigNode node) {
		final Optional<String> value = node.getValue();

		if (value.isPresent()) {
			assertThat(node.getPath(), value.get(), equalTo(node.getPath()));
		}

		node.getChildren().forEach(this::check);
	}

}
