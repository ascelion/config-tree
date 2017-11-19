
package ascelion.config.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

@ConfigReader.Type( "VAL" )
@ConfigSource( type = "VAL", priority = Integer.MIN_VALUE )
class VALConfigReader implements ConfigReader
{

	@Inject
	private ConfigExtension ext;

	@Override
	public Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys ) throws ConfigException
	{
		final Map<String, String> map = new HashMap<>();

		this.ext.values().forEach( a -> {
			final ConfigNodeImpl node = new ConfigNodeImpl();
			final String v = a.value();

			node.set( v );

			node.getKeys().forEach( k -> map.put( k, null ) );
		} );

		return map;
	}
}
