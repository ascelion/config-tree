
package ascelion.config.conv;

import java.lang.reflect.Proxy;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;

import static java.lang.String.format;

final class InterfaceConverter<T> implements ConfigConverter<T>
{

	final Class<T> type;
	final Converters conv;

	InterfaceConverter( Class<T> type, Converters cvs )
	{
		if( !type.isInterface() ) {
			throw new ConfigException( format( "Not a concrete interface: %s", type.getTypeName() ) );
		}

		this.type = type;
		this.conv = cvs;
	}

	@Override
	public T create( ConfigNode u, int unwrap )
	{
		final Class<?>[] types = new Class[] { this.type };
		final ClassLoader cld = this.type.getClassLoader();

		return (T) Proxy.newProxyInstance( cld, types, new InterfaceValue( this.type, this.conv, u ) );
	}

	@Override
	public T create( String u )
	{
		throw new UnsupportedOperationException();
	}

}
