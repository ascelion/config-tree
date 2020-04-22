
package ascelion.config.convert;

import ascelion.config.api.ConfigNode;
import ascelion.config.spi.ConfigConverter;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

final class MapConverter<M extends Map<String, T>, T> extends WrappedConverter<M, T>
{

	private final Supplier<M> sup;

	MapConverter( Supplier<M> sup, Type type, ConfigConverter<T> conv )
	{
		super( type, conv );

		this.sup = sup;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Optional<M> convert( ConfigNode node )
	{
		final Collection<ConfigNode> children = node.getChildren();
		final M map = this.sup.get();
		final String base = node.getPath();
		final int baseLen = base.length() + 1;

		fillMap( map, children, baseLen );

		return Optional.of( map );
	}

	private void fillMap( M map, Collection<ConfigNode> children, int baseLen )
	{
		children.forEach( child -> fillMap( map, child, baseLen ) );
	}

	private void fillMap( M map, ConfigNode child, int baseLen )
	{
		final Optional<T> opt = this.conv.convert( child );

		if( opt.isPresent() ) {
			final String key = child.getPath().substring( baseLen );

			map.put( key, opt.get() );
		}

		fillMap( map, child.getChildren(), baseLen );
	}
}
