
package ascelion.shared.cdi.conf;

import java.util.Map;
import java.util.function.Function;

public interface ConfigItem
{

	enum Kind
	{
		NONE,
		ITEM,
		TREE,
	}

	String getName();

	Kind getKind();

	String getItem();

	Map<String, ? extends ConfigItem> getTree();

	<T> Map<String, T> asMap( Function<String, T> fun );

	default <T> T getValue()
	{
		switch( getKind() ) {
			case ITEM:
				return (T) getItem();
			case TREE:
				return (T) getTree();

			default:
				return null;
		}
	}
}
