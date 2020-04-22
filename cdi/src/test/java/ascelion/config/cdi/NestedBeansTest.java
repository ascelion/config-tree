package ascelion.config.cdi;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import ascelion.config.api.ConfigPrefix;
import ascelion.config.api.ConfigValue;
import ascelion.config.core.AbstractTest;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;

public class NestedBeansTest extends AbstractTest {

	@ConfigValue
	@EqualsAndHashCode
	@NoArgsConstructor
	@AllArgsConstructor
	@Setter(onParam_ = @ConfigValue)
	static class User {
		String username;
		String password;
		String[] roles;
	}

	@ConfigPrefix("bean")
	static class Bean {
		@ConfigValue
		User[] users;
	}

	@Test
	public void run() {
		@SuppressWarnings("unchecked")
		final SeContainerInitializer weld = SeContainerInitializer.newInstance()
				.addExtensions(ConfigExtension.class)
				.addBeanClasses(Bean.class, User.class);

		try (SeContainer cont = weld.initialize()) {
			final Bean bean = cont.select(Bean.class).get();

			assertThat(bean, is(notNullValue()));
			assertThat(bean.users, is(notNullValue()));
			assertThat(asList(bean.users), hasSize(2));

			final User u1 = bean.users[0];

			assertThat(u1.username, equalTo("u1"));
			assertThat(u1.password, equalTo("p1"));
			assertThat(asList(u1.roles), equalTo(asList("role11", "role12")));

			final User u2 = bean.users[1];

			assertThat(u2.username, equalTo("u2"));
			assertThat(u2.password, equalTo("p2"));
			assertThat(asList(u2.roles), equalTo(asList("role21", "role22")));
		}
	}
}
