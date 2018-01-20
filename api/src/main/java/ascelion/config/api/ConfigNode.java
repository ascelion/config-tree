
package ascelion.config.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;

public interface ConfigNode
{

	String getName();

	String getPath();

	String getValue();

	String getRawValue();

	Collection<ConfigNode> getNodes();

	Set<String> getKeys();

	ConfigNode getNode( String path );

	String getValue( String path );

	void addChangeListener( PropertyChangeListener pcl );

	void removeChangeListener( PropertyChangeListener pcl );
}
