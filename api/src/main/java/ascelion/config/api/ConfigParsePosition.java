
package ascelion.config.api;

import java.io.Serializable;

import static java.lang.String.format;

import lombok.Getter;

@Getter
public final class ConfigParsePosition implements Serializable
{

	private final String message;
	private final int position;

	public ConfigParsePosition( String message, int position )
	{
		this.message = message;
		this.position = position;
	}

	@Override
	public String toString()
	{
		return format( "at %d: %s", this.position, this.message );
	}
}
