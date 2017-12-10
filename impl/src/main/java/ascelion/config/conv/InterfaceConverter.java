
package ascelion.config.conv;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;

import static java.lang.String.format;

final class InterfaceConverter<T> implements ConfigConverter<T>
{

	private final ConfigConverter<?> conv;

	public InterfaceConverter( ConfigConverter<?> conv )
	{
		this.conv = conv;
	}

	@Override
	public T create( Type t, ConfigNode u, int unwrap )
	{
		if( !( t instanceof Class ) ) {
			throw new ConfigException( format( "Not a concrete interface: %s", t.getTypeName() ) );
		}

		final Class<?> cls = (Class<?>) t;

		if( !cls.isInterface() ) {
			throw new ConfigException( format( "Not a concrete interface: %s", t.getTypeName() ) );
		}

		final Class<?>[] types = new Class[] { cls };
		final ClassLoader cld = cls.getClassLoader();

		return (T) Proxy.newProxyInstance( cld, types, new InterfaceValue( cls, this.conv, u ) );
	}

	@Override
	public T create( Type t, String u )
	{
		throw new UnsupportedOperationException();
	}

}
