
package ascelion.config.api;

import java.util.Map;
import java.util.Set;

public interface ConfigNode
{

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

	Kind getKind();

	/**
	 * See {@link Kind} for returning values.
	 */
	<T> T getValue();

	Set<String> getKeys();

	ConfigNode getNode( String path );

	<T> T getValue( String path );

	Map<String, String> asMap();
}
