
package ascelion.config.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRoot;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class ConfigRootBuilderTest
{

	@Test
	void simpleBuild()
	{
		final ConfigRoot root = new ConfigRootBuilder()
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

		assertThat( check( root, root ), equalTo( 9 ) );
	}

	@Test
	void secondBuild()
	{
		final ConfigRootImpl root1 = new ConfigRootBuilder()
		//@formatter:off
			.child("x.1")
				.child("1")
					.child("1").value("x.1.1.1")
					.back()
				.child("2").value("x.1.2")
				.back()
			.get();
		//@formatter:on

		final ConfigRootImpl root2 = new ConfigRootBuilder()
		//@formatter:off
			.child("x.2")
				.child("1").value("x.2.1")
				.child("2").value("x.2.2")
			.get();
		//@formatter:on

		root1.merge( root2, false );

		assertThat( check( root1, root1 ), equalTo( 9 ) );
	}

	private int check( ConfigNode root, ConfigNode node )
	{
		assertThat( node.getPath(), node.root(), sameInstance( root ) );

		final Optional<String> value = node.getValue();

		if( value.isPresent() ) {
			assertThat( node.getPath(), value.get(), equalTo( node.getPath() ) );
		}

		final int[] count = { 1 };

		node.getChildren().forEach( c -> count[0] += check( root, c ) );

		return count[0];
	}

}
