
package ascelion.config.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;

public interface ConfigNode
{

	@Deprecated
	enum Kind
	{

		/**
		 * The configuration value is null.
		 */
		NULL,

		/**
		 * The configuration is a string or an expression that evaluates to string, {@link ConfigNode#getValue()} returns {@link String}.
		 */
		ITEM,

		/**
		 * The configuration is an expression that evaluates to a different node, {@link ConfigNode#getValue()} returns {@link ConfigNode}.
		 */
		LINK,

		/**
		 * The configuration is a node container, {@link ConfigNode#getValue()} returns {@link java.util.Collection}&lt;{@link ConfigNode}&gt;.
		 */
		NODE,
	}

	String getName();

	String getPath();

	@Deprecated
	default Kind getKind()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * See {@link Kind} for returning values.
	 */
	<T> T getValue();

	Collection<ConfigNode> getNodes();

	Set<String> getKeys();

	ConfigNode getNode( String path );

	@Deprecated
	default <T> T getValue( String path )
	{
		return getNode( path ).getValue();
	}

	void addChangeListener( PropertyChangeListener pcl );

	void removeChangeListener( PropertyChangeListener pcl );
}
