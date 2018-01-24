
package ascelion.config.eclipse;

import java.lang.reflect.Type;

import org.eclipse.microprofile.config.Config;

public interface ConfigInternal extends Config
{

	String getValue( String propertyName );

	<T> T convert( String value, Type type );
}
