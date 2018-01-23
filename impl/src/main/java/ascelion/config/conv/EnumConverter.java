
package ascelion.config.conv;

import ascelion.config.api.ConfigConverter;

import static ascelion.config.conv.NullableConverter.nullable;

final class EnumConverter<E extends Enum<E>> implements ConfigConverter<E>
{

	static <E extends Enum<E>> ConfigConverter<E> enumeration( Class<E> type )
	{
		return nullable( new EnumConverter<>( type ) );
	}

	final Class<E> type;

	private EnumConverter( Class<E> type )
	{
		this.type = type;
	}

	@Override
	public E create( String u )
	{
		return Enum.valueOf( this.type, u );
	}

}
