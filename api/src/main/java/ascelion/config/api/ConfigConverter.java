
package ascelion.config.api;

import static java.util.Optional.ofNullable;

public interface ConfigConverter<T>
{

	default boolean isNullHandled()
	{
		return true;
	}

	default T create( ConfigNode u, int unwrap )
	{
		if( u == null ) {
			return create( null );
		}

		return create( ofNullable( u ).map( ConfigNode::getValue ).orElse( null ) );
	}

	T create( String u );
}
