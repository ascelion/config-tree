
package ascelion.config.conv;

import java.lang.reflect.Type;

import ascelion.config.api.ConfigConverter;

abstract class WrapConverter<C, T> implements ConfigConverter<C>
{

	final Type type;
	final ConfigConverter<T> conv;

	WrapConverter( Type type, ConfigConverter<T> conv )
	{
		this.type = type;
		this.conv = conv;
	}
}
