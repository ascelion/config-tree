
package ascelion.config.api;

import java.util.Collection;
import java.util.Map;

public interface ConfigNode
{

	String getName();

	String getPath();

	String getValue();

	String getValue( String path );

	Collection<? extends ConfigNode> getNodes();

	ConfigNode getNode( String path );

	Map<String, String> asMap( int unwrap );

	default Map<String, String> asMap()
	{
		return asMap( 0 );
	}

}
