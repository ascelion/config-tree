
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.Map;

import ascelion.config.api.ConfigConverter;
import ascelion.config.utils.References;
import ascelion.config.utils.Utils;

public final class ConverterRegistry
{

	private static ConverterRegistry instance;

	static public ConverterRegistry instance()
	{
		if( instance != null ) {
			return instance;
		}

		synchronized( ConverterRegistry.class ) {
			if( instance != null ) {
				return instance;
			}

			return instance = new ConverterRegistry();
		}
	}

	private final References<Converters> converters = new References<>();

	private ConverterRegistry()
	{
	}

	public void add( ConfigConverter<?> c )
	{
		add( null, c );
	}

	public void add( ClassLoader cld, ConfigConverter<?> c )
	{
		get( cld ).register( c );
	}

	public void add( Type t, ConfigConverter<?> c, int p )
	{
		add( null, t, c, p );
	}

	public void add( ClassLoader cld, Type t, ConfigConverter<?> c, int p )
	{
		get( cld ).register( t, c, p );
	}

	public ConfigConverter<?> getConverter( Type type )
	{
		return get( null ).getConverter( type );
	}

	public ConfigConverter<?> getConverter( ClassLoader cld, Type type )
	{
		return get( cld ).getConverter( type );
	}

	public Map<Type, ConfigConverter<?>> getConverters( ClassLoader cld )
	{
		return get( cld ).getConverters();
	}

	private Converters get( ClassLoader cld )
	{
		cld = Utils.classLoader( cld, getClass() );

		return this.converters.get( cld, x -> new Converters() );
	}
}
