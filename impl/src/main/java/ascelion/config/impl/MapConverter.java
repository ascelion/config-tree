
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static java.util.Collections.emptyMap;

final class MapConverter<T> implements ConfigConverter<Map<String, T>>
{

	private final ConfigConverter<T> conv;
	private final ConfigNode root;
	private final int unwrap;

	MapConverter( ConfigConverter<T> conv, ConfigNode root, int unwrap )
	{
		this.conv = conv;
		this.root = root;
		this.unwrap = unwrap;
	}

	@Override
	public Map<String, T> create( Type t, String u )
	{
		final String eval = Eval.eval( u, this.root );
		final ConfigNode node = this.root.getNode( eval != null ? eval : u );

		if( node == null ) {
			return emptyMap();
		}

		final Function<String, T> fun = x -> {
			if( x.contains( ItemTokenizer.Token.S_BEG ) ) {
				final EvalConverter<T> e = new EvalConverter<>( this.root, this.conv );

				return e.create( t, x );
			}
			else {
				return this.conv.create( t, x );
			}
		};

		return node.asMap( this.unwrap, fun );
	}

	@Override
	public Map<String, T> create( Class<? super Map<String, T>> t, String u )
	{
		final ParameterizedType p = (ParameterizedType) t.getGenericSuperclass();

		return create( p.getActualTypeArguments()[1], u );
	}
}
