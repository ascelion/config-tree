
package ascelion.config.impl;

import static java.lang.String.format;

public final class EvalError
{

	public final String message;
	public final int position;

	EvalError( String message, int position )
	{
		this.message = message;
		this.position = position;
	}

	public String toMessage()
	{
		return format( "At %d: %s", this.position, this.message );
	}

	@Override
	public String toString()
	{
		return toMessage();
	}
}
