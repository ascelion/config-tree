
package ascelion.config.api;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

public class ConfigParseException extends ConfigException
{

	private final List<ConfigParsePosition> errors;

	public ConfigParseException( String content, List<ConfigParsePosition> errors )
	{
		super( format( "'%s': %s", content, errors.get( 0 ).toMessage() ) );

		this.errors = unmodifiableList( new ArrayList<>( errors ) );
	}

	public List<ConfigParsePosition> getErrors()
	{
		return this.errors;
	}
}
