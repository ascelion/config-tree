
package ascelion.config.conv;

import java.lang.reflect.Type;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

class InternalConverter<T, X>
{

	static <T, X> InternalConverter<T, X> wrap( ConfigConverter<X> cvt )
	{
		return null;
	}

	T convert( Type t, ConfigNode n, InternalConverter<T, X> icv, int unwrap )
	{
		return null;
	}
}
