
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
		if( u == null ) {
			return create( t, null );
		}

		return create( t, ofNullable( u ).map( ConfigNode::getValue ).orElse( null ) );
	}

	T create( Type t, String u );
}
