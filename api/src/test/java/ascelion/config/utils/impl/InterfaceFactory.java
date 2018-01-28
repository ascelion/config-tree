
package ascelion.config.utils.impl;

import java.util.function.Function;

final class InterfaceFactory implements Function<ClassLoader, Interface>
{

	@Override
	public Interface apply( ClassLoader t )
	{
		return new Implementation();
	}
}
