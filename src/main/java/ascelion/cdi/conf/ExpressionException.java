
package ascelion.cdi.conf;

import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

public class ExpressionException extends RuntimeException
{

	private final List<ExpressionError> errors;

	ExpressionException( String value, List<ExpressionError> errors )
	{
		super( format( "Expression error: %s", value ) );

		this.errors = errors;
	}

	ExpressionException( String value )
	{
		this.errors = emptyList();
	}

	public List<ExpressionError> getErrors()
	{
		return this.errors;
	}

}
