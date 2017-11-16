
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.List;

import ascelion.config.api.ConfigException;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

public class EvalException extends ConfigException
{

	private final String content;
	private final List<EvalError> errors;

	EvalException( String content, List<EvalError> errors )
	{
		super( format( "'%s': %s", content, errors.get( 0 ).toMessage() ) );

		this.content = content;
		this.errors = unmodifiableList( new ArrayList<>( errors ) );
	}

	public String getContent()
	{
		return this.content;
	}

	public List<EvalError> getErrors()
	{
		return this.errors;
	}

}
