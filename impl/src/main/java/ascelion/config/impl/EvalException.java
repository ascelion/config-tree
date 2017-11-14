
package ascelion.config.impl;

import java.util.List;

import ascelion.config.api.ConfigException;

class EvalException extends ConfigException
{

	private final List<EvalError> errors;

	EvalException( List<EvalError> errors )
	{
		super( errors.get( 0 ).toMessage() );

		this.errors = errors;
	}

	public List<EvalError> getErrors()
	{
		return this.errors;
	}

}
