
package ascelion.config.eclipse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.Priority;

import static java.lang.String.format;

import org.eclipse.microprofile.config.spi.Converter;

final class ConverterInfo<T> implements Converter<T>
{

	static int getPriority( Class<?> cls )
	{
		for( ; cls != Object.class; cls = cls.getSuperclass() ) {
			final Priority ap = cls.getAnnotation( Priority.class );

			if( ap != null ) {
				return ap.value();
			}
		}

		return 100;
	}

	final Converter<T> delegate;
	final int priority;

	ConverterInfo( Converter<T> delegate, int priority )
	{
		this.delegate = delegate;
		this.priority = priority;
	}

	@Override
	public T convert( String value )
	{
		return this.delegate.convert( value );
	}

	static Type typeOf( Class<? extends Converter> cls )
	{
		final String base = cls.getName();
	
		for( Class<?> c = cls; c != Object.class; c = c.getSuperclass() ) {
			final Type[] giv = c.getGenericInterfaces();
	
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
}
