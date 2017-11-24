
package ascelion.config.impl;

import ascelion.config.api.ConfigValue;

public interface Interface<T>
{

	@ConfigValue.Default
	T values();
}
