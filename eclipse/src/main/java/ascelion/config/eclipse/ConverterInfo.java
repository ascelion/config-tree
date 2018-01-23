
package ascelion.config.eclipse;

import javax.annotation.Priority;

import org.eclipse.microprofile.config.spi.Converter;

final class ConverterInfo<T>
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
}
