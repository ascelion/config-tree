
package ascelion.config.api;

import java.lang.reflect.Type;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public interface ConfigConverter<T>
{

	default boolean isNullHandled()
	{
		return true;
	}

	default T create( Type t, ConfigNode u, int unwrap )
	{
		switch( u.getKind() ) {
			case ITEM:
				return create( t, ofNullable( u ).map( ConfigNode::<String> getValue ).orElse( null ) );

			default:
				throw new ConfigException( format( "Cannot convert %s to string", u.getPath() ) );
		}
	}

	T create( Type t, String u );
}
