package ascelion.config.microprofile;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import ascelion.config.api.ConfigProvider;
import ascelion.config.api.ConfigRoot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigRootInstance {
	static public ConfigRoot get(BeanManager bm) {
		final Set<Bean<?>> beans;

		try {
			beans = bm.getBeans(ConfigRoot.class);
		} catch (final IllegalStateException e) {
			return ConfigProvider.load().get();
		}

		if (beans.isEmpty()) {
			return ConfigProvider.load().get();
		}
		if (beans.size() > 1) {
			throw new AmbiguousResolutionException("Ambigous bean definition for ConfigRoot");
		}

		@SuppressWarnings("unchecked")
		final Bean<ConfigRoot> bean = (Bean<ConfigRoot>) beans.iterator().next();
		final CreationalContext<ConfigRoot> cc = bm.createCreationalContext(bean);

		return (ConfigRoot) bm.getReference(bean, ConfigRoot.class, cc);
	}

	static public ConfigRoot get() {
		try {
			return get(CDI.current().getBeanManager());
		} catch (final IllegalStateException e) {
		}

		return ConfigProvider.load().get();
	}
}
