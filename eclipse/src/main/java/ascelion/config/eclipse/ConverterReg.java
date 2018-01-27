
package ascelion.config.eclipse;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import ascelion.config.conv.Converters;
import ascelion.config.utils.ServiceInstance;

import org.eclipse.microprofile.config.spi.Converter;

public final class ConverterReg
{

	private final Map<Type, ConverterInfo<?>> map = new HashMap<>();

	public ConverterReg discover( ClassLoader cld )
	{
		cld = ServiceInstance.classLoader( cld, getClass() );

		ServiceLoader.load( Converter.class, cld ).forEach( c -> addConverter( new ConverterInfo<>( c ) ) );

		return this;
	}

	ConverterReg addConverter( ConverterInfo<?> c )
	{
		this.map.compute( c.type, ( t, i ) -> {
			if( i == null || i.priority < c.priority ) {
				return c;
			}
			else {
				return i;
			}
		} );

		return this;
	}

	public Converters get()
	{
		final Converters cvs = new Converters();

		this.map.values().forEach( i -> cvs.register( i.type, i.converter::convert ) );

		return cvs;
	}
}
