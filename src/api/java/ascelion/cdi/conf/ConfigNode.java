
package ascelion.cdi.conf;

import java.util.Map;
import java.util.function.Function;

public interface ConfigNode
{

	String getName();

	String getPath();

	String getValue();

	void setValue( String value );

	void setValues( Map<String, ?> values );

	ConfigNode getNode( String path );

	String getValue( String path );

	void setValue( String path, String value );

	void setValues( String path, Map<String, ?> values );

	<T> Map<String, T> asMap( Function<String, T> fun );
}
