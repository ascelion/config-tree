
package ascelion.config.convert;

import static ascelion.config.spi.Utils.isArrayNode;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

final class ArrayConverter<T> extends WrappedConverter<Object[], T>
{

	ArrayConverter( Type type, ConfigConverter<T> conv )
	{
		super( type, conv );
	}

	@Override
	public Optional<Object[]> convert( ConfigNode node )
	{
		final Collection<ConfigNode> children = node.getChildren();
		final Stream<Optional<T>> stream;

		if( isArrayNode( node.getChildren() ) ) {
			stream = children.stream().map( this.conv::convert );
		}
		else if( node.getValue().isPresent() ) {
			stream = Stream.of( this.conv.convert( node ) );
		}
		else {
			stream = Stream.empty();
		}

		final Object[] result = stream
			.filter( Optional::isPresent )
			.map( Optional::get )
			.toArray( this::newArray );

		return Optional.of( result );
	}

	private Object[] newArray( int n )
	{
		return this.type instanceof Class
			? (Object[]) Array.newInstance( (Class<?>) this.type, n )
			: (Object[]) Array.newInstance( Object.class, n );
	}
}
