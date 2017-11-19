
package ascelion.config.api;

import static java.lang.String.format;

public class ConfigNotFoundException extends ConfigException
{

	public ConfigNotFoundException( String path )
	{
		super( format( "Configuration node not found: %s", path ) );
	}
}
