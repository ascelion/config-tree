
package ascelion.config.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;

public final class Utils
{

	static public Stream<Method> sMethodsOf( Class<?> cls )
	{
		return Stream.of( cls.getMethods() );
	}

	static public Set<Method> methodsOf( Class<?> cls )
	{
		return sMethodsOf( cls ).collect( Collectors.toSet() );
	}

	static public String[] toArray( String value )
	{
		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}

	static public Stream<String> toStream( String value )
	{
		return Stream.of( toArray( value ) );
	}

	static public String[] keys( String path )
	{
		return path != null ? path.split( "\\." ) : new String[0];
	}

	static public String path( int s, int e, String[] keys )
	{
		return asList( keys ).subList( s, e ).stream().collect( Collectors.joining( "." ) );
	}

	static public String path( String... names )
	{
		return Stream.of( names ).filter( StringUtils::isNotBlank ).collect( Collectors.joining( "." ) );
	}

	static public <A extends Annotation> Optional<A> findAnnotation( Class<A> annotation, Class<?> cls )
	{
		if( cls == Object.class ) {
			return Optional.ofNullable( null );
		}

		if( cls.isAnnotationPresent( annotation ) ) {
			return Optional.of( cls.getAnnotation( annotation ) );
		}

		return findAnnotation( annotation, cls.getSuperclass() );
	}

	private Utils()
	{
	}
}
