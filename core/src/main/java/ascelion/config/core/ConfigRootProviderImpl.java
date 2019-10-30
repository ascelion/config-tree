package ascelion.config.core;

import ascelion.config.api.ConfigRoot;
import ascelion.config.spi.ConfigRootProvider;

public class ConfigRootProviderImpl extends ConfigRootProvider {
	private final ConfigRoot root = new ConfigRootImpl();

	@Override
	protected ConfigRoot get() {
		return this.root;
	}
}
