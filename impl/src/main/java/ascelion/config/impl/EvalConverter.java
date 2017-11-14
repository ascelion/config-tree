
package ascelion.config.impl;

import java.lang.reflect.Type;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class EvalConverter<T> implements ConfigConverter<T>
{

	private final ConfigNode root;
	private final ConfigConverter<T> conv;

	EvalConverter( ConfigNode root, ConfigConverter<T> conv )
	{
		this.root = root;
		this.conv = conv;
	}

	@Override
	public T create( Class<? super T> t, String u )
	{
		return create( (Type) t, u );
	}

	@Override
	public T create( Type t, String u )
	{
		String v = Eval.eval( u, this.root );

		if( isBlank( v ) && t instanceof Class && ( (Class) t ).isPrimitive() ) {
			v = "0";
		}

		return isNotBlank( v ) ? this.conv.create( t, v ) : null;
	}

}
