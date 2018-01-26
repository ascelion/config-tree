
package ascelion.config.eclipse.cdi;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.InjectionPoint;

import ascelion.config.eclipse.ext.ConfigExt;

import org.eclipse.microprofile.config.inject.ConfigProperty;

final class ConfigPropertyFactory
{

	@Typed
	@ConfigProperty
	static Object getValue( InjectionPoint ip, ConfigExt cf )
	{
		final String pn = propertyName( ip );
		String pv = cf.getValue( pn );

		if( pv == null ) {
			final ConfigProperty cp = ip.getAnnotated().getAnnotation( ConfigProperty.class );

			if( !ConfigProperty.UNCONFIGURED_VALUE.equals( cp.defaultValue() ) ) {
				pv = cp.defaultValue();
			}
		}

		return cf.convert( pv, ip.getType() );
	}

	static String propertyName( InjectionPoint ip )
	{
		final Annotated annotated = ip.getAnnotated();
		final String name = annotated.getAnnotation( ConfigProperty.class ).name();

		if( name.length() > 0 ) {
			return name;
		}

		if( annotated instanceof AnnotatedParameter ) {
			final Parameter p = ( (AnnotatedParameter<?>) annotated ).getJavaParameter();

			if( p.isNamePresent() ) {
				return p.getDeclaringExecutable().getDeclaringClass().getCanonicalName() + "." + p.getName();
			}
		}
		if( annotated instanceof AnnotatedField ) {
			final Field f = ( (AnnotatedField) annotated ).getJavaMember();

			return f.getDeclaringClass().getCanonicalName() + "." + f.getName();
		}
		if( annotated instanceof AnnotatedMethod ) {
			final Method m = ( (AnnotatedMethod) annotated ).getJavaMember();

			if( m.getParameterTypes().length == 1 ) {
				String n = m.getName();

				if( n.startsWith( "set" ) ) {
					n = Introspector.decapitalize( n.substring( 3 ) );
				}

				return m.getDeclaringClass().getCanonicalName() + "." + n;
			}
		}

		throw new IllegalStateException( "Could not find default name for @ConfigProperty InjectionPoint " + ip );
	}
}
