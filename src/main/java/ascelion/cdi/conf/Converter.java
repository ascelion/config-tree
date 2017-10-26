
package ascelion.cdi.conf;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import ascelion.shared.cdi.conf.ConfigNode;

final class Converter
{

	private final ConfigNode root;
	private final String item;
	private final BiFunction<Class<?>, String, ?> func;
	private final Type type;

	Converter( ConfigNode root, String item, BiFunction<Class<?>, String, ?> func, Type type )
	{
		this.root = root;
		this.item = item;
		this.func = func;
		this.type = type;
	}

	private Converter( Converter parent, Type type )
	{
		this.root = parent.root;
		this.item = parent.item;
		this.func = parent.func;
		this.type = type;
	}

	Object convert()
	{
		if( this.type instanceof Class ) {
			final Class<?> cls = (Class<?>) this.type;

			if( cls.isArray() ) {

			}
		}
		return null;
	}
}
