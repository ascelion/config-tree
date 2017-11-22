
package ascelion.config.impl;

import ascelion.config.api.ConfigException;

public final class ConfigLoopException extends ConfigException
{

	public ConfigLoopException( String message )
	{
		super( message );
	}

}
