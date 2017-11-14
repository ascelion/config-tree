
package ascelion.config.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.GenericsContext;

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
		return isBlank( path ) ? new String[0] : path.split( "\\." );
	}

	static public String path( ConfigNode node )
	{
		return node != null ? node.getPath() : null;
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

	static Type converterType( final Class<? extends ConfigConverter> cls )
	{
		final GenericsContext c1 = GenericsResolver.resolve( cls );
		final GenericsContext c2 = c1.type( ConfigConverter.class );
		Type t = c2.genericType( 0 );

		if( t instanceof GenericArrayType ) {
			final GenericArrayType gat = (GenericArrayType) t;
			final Type gct = gat.getGenericComponentType();

			if( gct instanceof Class<?> ) {
				t = Array.newInstance( (Class<?>) gct, 0 ).getClass();
			}
		}

		return t;
	}
}
