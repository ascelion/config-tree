
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.ServiceLoader;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigSource;
import ascelion.config.utils.Utils;

public final class ConfigJava
{

	private final ConfigLoad ld = new ConfigLoad();
	private ConfigNode root;

	public void add( ConfigReader rd )
	{
		this.ld.addReader( rd );
	}

	public void add( ConfigSource cs )
	{
		this.ld.addSource( cs );
	}

	public void add( ConfigConverter<?> cv )
	{
		ConfigRegistry.getInstance().converters( null ).register( cv );
	}

	public void add( Type type, ConfigConverter<?> cv )
	{
		ConfigRegistry.getInstance().converters( null ).register( type, cv, Utils.getPriority( cv ) );
	}

	public ConfigNode root()
	{
		if( this.root == null ) {
			ServiceLoader.load( ConfigConverter.class )
				.forEach( ConfigRegistry.getInstance().converters( null )::register );

			this.root = this.ld.load();
		}

		return this.root;
	}

}
