
package ascelion.config.eclipse.ext;

import java.lang.reflect.Type;

import org.eclipse.microprofile.config.Config;

public interface ConfigExt extends Config
{

	String getValue( String propertyName );

	String getValue( String propertyName, boolean evaluate );

	<T> T convert( String value, Type type );
}
