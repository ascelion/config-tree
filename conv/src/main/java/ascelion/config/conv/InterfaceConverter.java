
package ascelion.config.conv;

import java.lang.reflect.Proxy;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;

import static java.lang.String.format;

final class InterfaceConverter<T> implements ConfigConverter<T>
{

	final Class<T> type;

	InterfaceConverter( Class<T> type )
	{
		if( !type.isInterface() ) {
			throw new ConfigException( format( "Not a concrete interface: %s", type.getTypeName() ) );
		}

		this.type = type;
	}

	@Override
	public T create( ConfigNode u, int unwrap )
	{
		final Class<?>[] types = new Class[] { this.type };
		final ClassLoader cld = this.type.getClassLoader();

		return (T) Proxy.newProxyInstance( cld, types, new InterfaceValue( this.type, u ) );
	}

	@Override
	public T create( String u )
	{
		throw new UnsupportedOperationException();
	}

}
