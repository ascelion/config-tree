
package ascelion.config.conv;

import java.lang.reflect.Type;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNode.Kind;

import static org.apache.commons.lang3.StringUtils.isBlank;

final class PrimitiveConverter<T> implements ConfigConverter<T>
{

	static <T> ConfigConverter<T> primitive( ConfigConverter<T> conv )
	{
		return new PrimitiveConverter<>( conv );
	}

	private PrimitiveConverter( ConfigConverter<T> conv )
	{
		this.conv = conv;
	}

	private final ConfigConverter<T> conv;

	@Override
	public T create( Type t, String u )
	{
		if( isBlank( u ) ) {
			return this.conv.create( t, "0" );
		}

		return this.conv.create( t, u );
	}

	@Override
	public T create( Type t, ConfigNode u, int unwrap )
	{
		if( u == null || u.getKind() == Kind.NULL ) {
			return this.conv.create( t, "0" );
		}

		return this.conv.create( t, u, unwrap );
	}
}
