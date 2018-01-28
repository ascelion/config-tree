
package ascelion.config.utils.impl;

import ascelion.config.utils.ServiceInstance.CLD;

final class Implementation implements Interface
{

	@CLD
	private ClassLoader cld;

	@Override
	public ClassLoader getClassLoader()
	{
		return this.cld;
	}
}
