
package ascelion.config.convert;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class EnumConverter<E extends Enum<E>> implements ConfigConverter<E>
{

	private final Class<E> type;

	@Override
	public Optional<E> convert( ConfigNode node )
	{
		return node.getValue()
			.map( v -> Enum.valueOf( this.type, v ) );
	}
}
