package ascelion.config.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRoot;

import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

public class ConfigLinksTest {

	@Test
	public void case1() {
		final ConfigRootBuilder bld = new ConfigRootBuilder();
		final ConfigRoot root = bld
//@formatter:off
			.child("X")
				.child("a")
					.child("1").value("a1")
					.child("2").value("a2")
					.back()
				.child("b").value("${X.a}")
				.child("c").value("${X.b}")
		.get();
//@formatter:on

		checkNodes(root);
	}

	@Test
	public void case2() {
		final ConfigRootBuilder bld = new ConfigRootBuilder();
		final ConfigRoot root = bld
//@formatter:off
				.set("X.a.1", "a1")
				.set("X.a.2", "a2")
				.set("X.b", "X.${B}")
				.set("X.c", "X.${C}")
				.set("B", "a")
				.set("C", "b")
				.get();
//@formatter:on

		checkNodes(root);
	}

	@Test
	public void case3() {
		final ConfigRootBuilder bld = new ConfigRootBuilder();
		final ConfigRoot root = bld
//@formatter:off
				.set("X.a.1", "a1")
				.set("X.a.2", "a2")
				.set("X.b", "${X.${B}}")
				.set("X.c", "${X.${C}}")
				.set("B", "a")
				.set("C", "b")
				.get();
//@formatter:on

		checkNodes(root);
	}

	private void checkNodes(ConfigRoot root) {
		final ConfigNode a = root.getValue("X.a", ConfigNode.class).get();
		final ConfigNode b = root.getValue("X.b", ConfigNode.class).get();
		final ConfigNode c = root.getValue("X.c", ConfigNode.class).get();

		final Collection<ConfigNode> ca = a.getChildren();
		final Collection<ConfigNode> cb = b.getChildren();
		final Collection<ConfigNode> cc = c.getChildren();

		assertThat(ca.size(), equalTo(cb.size()));
		assertThat(cb.size(), equalTo(cc.size()));

		for (Iterator<ConfigNode> ia = ca.iterator(), ib = cb.iterator(), ic = cc.iterator(); ia.hasNext();) {
			final ConfigNode na = ia.next();
			final ConfigNode nb = ib.next();
			final ConfigNode nc = ic.next();

			assertThat(na, sameInstance(nb));
			assertThat(nb, sameInstance(nc));
		}
	}
}
