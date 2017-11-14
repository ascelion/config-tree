
package ascelion.config.impl;

import java.util.ServiceLoader;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;

public final class ConfigJava
{

	private final ConfigScanner sc = new ConfigScanner();
	private final ConfigLoad ld = new ConfigLoad();
	private final Converters cvs = new Converters();
	private ConfigNode root;

	public ConfigJava()
	{
	}

	public ConfigNode root()
	{
		if( this.root == null ) {
			ServiceLoader.load( ConfigReader.class )
				.forEach( this.ld::addReader );
			ServiceLoader.load( ConfigConverter.class )
				.forEach( this.cvs::register );

			this.root = this.ld.load( this.sc.getSources() );
		}

		return this.root;
	}

	public <T> T getValue( Class<T> type, String prop )
	{
		final ConfigValueLiteral a = new ConfigValueLiteral( prop );
		final TypedValue v = new TypedValue( root(), a, type, t -> this.cvs );

		return (T) v.get();
	}

}
