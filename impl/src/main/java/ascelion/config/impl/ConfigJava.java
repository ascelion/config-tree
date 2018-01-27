
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.ServiceLoader;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.conv.ConverterRegistry;
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
		ConverterRegistry.instance().add( cv );
	}

	public void add( Type type, ConfigConverter<?> cv )
	{
		ConverterRegistry.instance().add( type, cv, Utils.getPriority( cv ) );
	}

	public ConfigNode root()
	{
		if( this.root == null ) {
			ServiceLoader.load( ConfigConverter.class )
				.forEach( ConverterRegistry.instance()::add );

			this.root = this.ld.load();
		}

		return this.root;
	}

}
