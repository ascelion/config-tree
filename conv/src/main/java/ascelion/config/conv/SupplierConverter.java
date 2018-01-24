
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.function.Supplier;

import ascelion.config.api.ConfigConverter;

final class SupplierConverter<T> extends WrapConverter<Supplier<T>, T>
{

	SupplierConverter( Type type, ConfigConverter<T> conv )
	{
		super( type, conv );

	}

	@Override
	public Supplier<T> create( String u )
	{
		return () -> this.conv.create( u );
	}
}
