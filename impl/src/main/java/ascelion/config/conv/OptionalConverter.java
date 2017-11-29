
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.Optional;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static java.util.Optional.ofNullable;

final class OptionalConverter<T> implements ConfigConverter<Optional<T>>
{

	private final Type type;
	private final ConfigConverter<T> conv;

	OptionalConverter( Type type, ConfigConverter<T> conv )
	{
		this.type = type;
		this.conv = conv;
	}

	@Override
	public Optional<T> create( Type t, ConfigNode u, int unwrap )
	{
		return ofNullable( this.conv.create( this.type, u, unwrap ) );
	}

	@Override
	public Optional<T> create( Type t, String u )
	{
		return ofNullable( this.conv.create( this.type, u ) );
	}

	@Override
	public boolean isNullHandled()
	{
		return true;
	}

}
