
package ascelion.config.conv;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

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
	public T create( String u )
	{
		if( isBlank( u ) ) {
			return this.conv.create( "0" );
		}

		return this.conv.create( u );
	}

	@Override
	public T create( ConfigNode u, int unwrap )
	{
		if( u == null ) {
			return this.conv.create( "0" );
		}

		return this.conv.create( u, unwrap );
	}
}
