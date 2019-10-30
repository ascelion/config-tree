package ascelion.config.core;

import java.util.Optional;

import ascelion.config.api.ConfigRoot;

class ConfigRootImpl extends ConfigNodeImpl implements ConfigRoot {
	@Override
	public <T> Optional<T> getValue(String expression, Class<T> type) {
		return null;
	}
}
