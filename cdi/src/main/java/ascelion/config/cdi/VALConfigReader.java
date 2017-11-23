
package ascelion.config.cdi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.Expression;

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

		addAll( map, keys );

		this.ext.values().forEach( a -> {
			addAll( map, Expression.compile( a.value() ).evaluables() );
		} );

		return map;
	}

	private void addAll( Map<String, String> map, Set<String> evaluables )
	{
		evaluables.forEach( e -> map.put( e, null ) );
	}
}
