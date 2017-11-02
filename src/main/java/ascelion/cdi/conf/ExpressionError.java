
package ascelion.cdi.conf;

public final class ExpressionError
{

	public final String message;
	public final int position;

	public ExpressionError( String message, int position )
	{
		this.message = message;
		this.position = position;
	}
}
