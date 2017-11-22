
package ascelion.config.cvt;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static java.lang.String.format;

final class InterfaceConverter<T> implements ConfigConverter<T>
{

	private final Converters conv;

	public InterfaceConverter( Converters conv )
	{
		this.conv = conv;
	}

	@Override
	public T create( Type t, ConfigNode u, int unwrap )
	{
		if( !( t instanceof Class ) ) {
			throw new IllegalArgumentException( format( "Not a concrete interface: %s", t.getTypeName() ) );
		}

		final Class<?> cls = (Class<?>) t;
		final Class<?>[] types = new Class[] { cls };
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();

		return (T) Proxy.newProxyInstance( cld, types, new InterfaceValue( cls, this.conv, u ) );
	}

	@Override
	public T create( Type t, String u, int unwrap )
	{
//		return create( t, this.conv.node( u ), unwrap );
		throw new UnsupportedOperationException();
	}

}
