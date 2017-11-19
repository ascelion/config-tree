
package ascelion.config.impl;

import java.lang.reflect.Proxy;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

class InterfaceConverter<T> implements ConfigConverter<T>
{

	private final ConfigNode root;
	private final Converters conv;

	InterfaceConverter( Converters conv, ConfigNode root )
	{
		this.root = root;
		this.conv = conv;
	}

	@Override
	public T create( Class<? super T> t, String u )
	{
		final Class[] types = new Class[] { t };
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();

		return (T) Proxy.newProxyInstance( cld, types, new InterfaceValue( t, this.root, u, this.conv ) );
	}

}
