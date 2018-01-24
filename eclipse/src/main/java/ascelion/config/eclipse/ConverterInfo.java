
package ascelion.config.eclipse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.Priority;

import static java.lang.String.format;

import org.eclipse.microprofile.config.spi.Converter;

final class ConverterInfo<T>
{

	static Type typeOf( Converter<?> c )
	{
		final Class<? extends Converter> base = c.getClass();

		for( Class<?> cls = base; cls != Object.class; cls = cls.getSuperclass() ) {
			final Type[] giv = cls.getGenericInterfaces();

			for( final Type gi : giv ) {
				if( gi instanceof ParameterizedType ) {
					final ParameterizedType pt = (ParameterizedType) gi;

					if( pt.getRawType().equals( Converter.class ) ) {
						final Type[] tav = pt.getActualTypeArguments();

						if( tav.length == 1 ) {
							return tav[0];
						}

					}
				}
			}
		}

		throw new IllegalStateException( format( "Cannot infer type from %s", base ) );
	}

	static int getPriority( Converter<?> c )
	{
		for( Class<?> cls = c.getClass(); cls != Object.class; cls = cls.getSuperclass() ) {
			final Priority ap = cls.getAnnotation( Priority.class );

			if( ap != null ) {
				return ap.value();
			}
		}

		return 100;
	}

	final Converter<T> converter;
	final int priority;
	final Type type;

	ConverterInfo( Converter<T> c )
	{
		this( c, getPriority( c ), typeOf( c ) );
	}

	ConverterInfo( Converter<T> converter, int priority, Type type )
	{
		this.converter = converter;
		this.priority = priority;
		this.type = type;
	}

}
