
package ascelion.config.api;

import static java.lang.String.format;

public class ConfigNotFoundException extends ConfigException
{

	public ConfigNotFoundException( String path )
	{
		super( format( "Configuration item not found: %s", path ) );
	}
}
