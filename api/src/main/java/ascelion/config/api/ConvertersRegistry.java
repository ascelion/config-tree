
package ascelion.config.api;

import java.lang.reflect.Type;
import java.util.Map;

public interface ConvertersRegistry
{

	void register( ConfigConverter<?> c );

	void register( Type t, ConfigConverter<?> c, int p );

	ConfigConverter<?> getConverter( Type type );

	Map<Type, ConfigConverter<?>> getConverters();
}
