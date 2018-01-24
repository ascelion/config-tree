
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.Optional;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static java.util.Optional.ofNullable;

final class OptionalConverter<T> extends WrapConverter<Optional<T>, T>
{

	OptionalConverter( Type type, ConfigConverter<T> conv )
	{
		super( type, conv );
	}

	@Override
	public Optional<T> create( ConfigNode u, int unwrap )
	{
		return ofNullable( this.conv.create( u, unwrap ) );
	}

	@Override
	public Optional<T> create( String u )
	{
		return ofNullable( this.conv.create( u ) );
	}

	@Override
	public boolean isNullHandled()
	{
		return true;
	}

}
