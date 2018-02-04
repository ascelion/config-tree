
package ascelion.config.api;

import java.lang.reflect.Type;
import java.util.Map;

import ascelion.config.utils.ServiceInstance;

public interface ConvertersRegistry
{

	class Instance
	{

		static private final ServiceInstance<ConvertersRegistry> si = new ServiceInstance<>( ConvertersRegistry.class );
	}

	static ConvertersRegistry newInstance( ClassLoader cld )
	{
		return Instance.si.create( cld );
	}

	static ConvertersRegistry getInstance()
	{
		return Instance.si.get();
	}

	static public void setInstance( ConvertersRegistry instance )
	{
		Instance.si.set( instance );
	}

	static ConvertersRegistry getInstance( ClassLoader cld )
	{
		return Instance.si.get( cld );
	}

	static public void setInstance( ClassLoader cld, ConvertersRegistry instance )
	{
		Instance.si.set( cld, instance );
	}

	static void reset()
	{
		Instance.si.clear();
	}

	void register( ConfigConverter<?> c );

	void register( Type t, ConfigConverter<?> c, int p );

	ConfigConverter<?> getConverter( Type type );

	Map<Type, ConfigConverter<?>> getConverters();
}
