
package ascelion.config.api;

import java.lang.reflect.Type;

public interface ConfigConverter<T>
{

	default T create( Type t, String u )
	{
		return create( (Class<? super T>) t, u );
	}

	T create( Class<? super T> t, String u );
}
