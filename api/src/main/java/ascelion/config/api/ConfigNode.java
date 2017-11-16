
package ascelion.config.api;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public interface ConfigNode
{

	String getName();

	String getPath();

	String getValue();

	Collection<? extends ConfigNode> getNodes();

	default String getValue( String path )
	{
		final ConfigNode node = getNode( path );

		return node != null ? node.getValue() : null;
	}

	ConfigNode getNode( String path );

	<T> Map<String, T> asMap( int unwrap, Function<String, T> fun );

	default <T> Map<String, T> asMap( Function<String, T> fun )
	{
		return asMap( 0, fun );
	}

}
