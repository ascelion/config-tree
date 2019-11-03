package ascelion.config.read;

import ascelion.config.api.ConfigProvider.Builder;
import ascelion.config.spi.ConfigInput;

class EnvironmentInput implements ConfigInput {
	@Override
	public int priority() {
		return 300;
	}

	@Override
	public void update(Builder bld) {
		bld.set(System.getenv());
	}
}
