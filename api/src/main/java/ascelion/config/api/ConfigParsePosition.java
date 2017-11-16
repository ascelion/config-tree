
package ascelion.config.api;

import static java.lang.String.format;

public class ConfigParsePosition
{

	private final String message;
	private final int position;

	public ConfigParsePosition( String message, int position )
	{
		this.message = message;
		this.position = position;
	}

	public String getMessage()
	{
		return this.message;
	}

	public int getPosition()
	{
		return this.position;
	}

	public String toMessage()
	{
		return format( "at %d: %s", this.position, this.message );
	}
}
