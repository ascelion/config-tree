
package ascelion.config.cvt;

import java.lang.reflect.Type;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNode.Kind;

import static org.apache.commons.lang3.StringUtils.isBlank;

final class NullableConverter<T> implements ConfigConverter<T>
{

	static <T> ConfigConverter<T> wrap( ConfigConverter<T> conv )
	{
		return new NullableConverter<>( conv );
	}

	private NullableConverter( ConfigConverter<T> conv )
	{
		this.conv = conv;
	}

	private final ConfigConverter<T> conv;

	@Override
	public T create( Type t, String u, int unwrap )
	{
		if( isBlank( u ) ) {
			return null;
		}

		return this.conv.create( t, u, unwrap );
	}

	@Override
	public T create( Type t, ConfigNode u, int unwrap )
	{
		if( u == null || u.getKind() == Kind.NULL ) {
			return null;
		}

		return this.conv.create( t, u, unwrap );
	}

	@Override
	public boolean isNullHandled()
	{
		return true;
	}
}
