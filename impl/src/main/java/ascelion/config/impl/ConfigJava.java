
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.ServiceLoader;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.conv.Converters;

public final class ConfigJava
{

	private final ConfigLoad ld = new ConfigLoad();
	private final Converters cvs = new Converters();

	private ConfigNode root;
	{
		this.cvs.setRootNode( this::root );
	}

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
		this.cvs.register( cv );
	}

	public void add( Type type, ConfigConverter<?> cv )
	{
		this.cvs.register( type, cv );
	}

	public ConfigNode root()
	{
		if( this.root == null ) {
			ServiceLoader.load( ConfigConverter.class )
				.forEach( this.cvs::register );

			this.root = this.ld.load();
		}

		return this.root;
	}

	public Converters getConverter()
	{
		return this.cvs;
	}
}
