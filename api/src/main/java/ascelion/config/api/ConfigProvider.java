
package ascelion.config.api;

import java.util.Map;

public abstract class ConfigProvider
{

	public interface Builder
	{

		Builder child();

		Builder child( String path );

		Builder value( String value );

		Builder set( Map<String, String> properties );

		Builder set( String path, String value );

		Builder back();

		ConfigRoot get();
	}

	static public ConfigRoot root()
	{
		return new Service<>( ConfigProvider.class ).load().get();
	}

	static public ConfigRoot root( ClassLoader cld )
	{
		return new Service<>( ConfigProvider.class ).load().get( cld );
	}

	protected ConfigRoot get()
	{
		return get( Thread.currentThread().getContextClassLoader() );
	}

	protected abstract ConfigRoot get( ClassLoader cld );
}
