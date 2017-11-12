
package ascelion.config.impl;

import java.util.List;

import static java.util.Collections.singletonList;

public class EvalException extends RuntimeException
{

	private final List<EvalError> errors;

	EvalException( List<EvalError> errors )
	{
		super( errors.get( 0 ).toMessage() );

		this.errors = errors;
	}

	EvalException( String message )
	{
		this( singletonList( new EvalError( message, 0 ) ) );
	}

	public List<EvalError> getErrors()
	{
		return this.errors;
	}

}
