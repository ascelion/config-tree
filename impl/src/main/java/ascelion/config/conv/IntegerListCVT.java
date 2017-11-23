
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.List;

import ascelion.config.api.ConfigNode;

class IntegerListCVT extends InternalConverter<List<String>, String>
{

	@Override
	List<String> convert( Type t, ConfigNode n, InternalConverter<List<String>, String> icv, int unwrap )
	{
		return super.convert( t, n, icv, unwrap );
	}
}
