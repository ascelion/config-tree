
package ascelion.config.conv;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static org.apache.commons.lang3.StringUtils.isBlank;

final class NullableConverter<T> implements ConfigConverter<T>
{

	static <T> ConfigConverter<T> nullable( ConfigConverter<T> conv )
	{
		return new NullableConverter<>( conv );
	}

	private NullableConverter( ConfigConverter<T> conv )
	{
		this.conv = conv;
	}

	private final ConfigConverter<T> conv;

	@Override
	public T create( String u )
	{
		if( isBlank( u ) ) {
			return null;
		}

		return this.conv.create( u );
	}

	@Override
	public T create( ConfigNode u, int unwrap )
	{
		if( u == null ) {
			return null;
		}

		return this.conv.create( u, unwrap );
	}
}
