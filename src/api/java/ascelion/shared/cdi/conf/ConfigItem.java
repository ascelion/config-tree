
package ascelion.shared.cdi.conf;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public interface ConfigItem
{

	static String fullPath( String... names )
	{
		return Stream.of( names ).filter( StringUtils::isNotBlank ).collect( Collectors.joining( "." ) );
	}

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

	<T> Map<String, T> asMap( String path, Function<String, T> fun );

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
