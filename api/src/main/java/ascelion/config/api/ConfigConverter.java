
package ascelion.config.api;

import java.lang.reflect.Type;

public interface ConfigConverter<T>
{

	default boolean isNullHandled()
	{
		return false;
	}

	default T create( Type t, ConfigNode u, int unwrap )
	{
		return create( t, (String) u.getValue(), unwrap );
	}

	default T create( Type t, ConfigNode u )
	{
		return create( t, u, 0 );
	}

	default T create( Type t, String u )
	{
		return create( t, u, 0 );
	}

	T create( Type t, String u, int unwrap );
}
