
package ascelion.config.api;

import java.lang.reflect.Type;

import static java.util.Optional.ofNullable;

public interface ConfigConverter<T>
{

	default boolean isNullHandled()
	{
		return true;
	}

	default T create( Type t, ConfigNode u, int unwrap )
	{
		return create( t, ofNullable( u ).map( ConfigNode::<String> getValue ).orElse( null ) );
	}

	T create( Type t, String u );
}
