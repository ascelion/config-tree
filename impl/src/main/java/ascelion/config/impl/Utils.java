
package ascelion.config.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.GenericsContext;

public final class Utils
{

	static public <X> X[] asArray( X... x )
	{
		return x;
	}

	static public <X> Set<? super X> asSet( X... x )
	{
		return asList( x ).stream().collect( Collectors.toSet() );
	}

	static public Set<Method> methodsOf( Class<?> cls )
	{
		return Stream.of( cls.getMethods() ).collect( Collectors.toSet() );
	}

	static public String[] values( String value )
	{
		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}

	static public String[] keys( String path )
	{
		return isNotBlank( path ) ? path.split( "\\." ) : new String[0];
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

	static public String unwrap( String path, int count )
	{
		final StringBuilder b = new StringBuilder( path );
		int u = count;

		while( u-- > 0 ) {
			final int x = b.indexOf( "." );

			if( x < 0 ) {
				throw new ConfigException( format( "Cannot unwrap %d items from %s", count, path ) );
			}

			b.delete( 0, x + 1 );
		}

		return b.toString();
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

	public static <T> Type paramType( final Class<? extends T> type, Class<T> base, int position )
	{
		final GenericsContext c1 = GenericsResolver.resolve( type );
		final GenericsContext c2 = c1.type( base );
		Type t = c2.genericType( position );

		if( t instanceof GenericArrayType ) {
			final GenericArrayType gat = (GenericArrayType) t;
			final Type gct = gat.getGenericComponentType();

			if( gct instanceof Class<?> ) {
				t = Array.newInstance( (Class<?>) gct, 0 ).getClass();
			}
			else {
				t = gct;
			}
		}

		return t;
	}

	static public boolean isPrimitive( Type t )
	{
		return t instanceof Class && ( (Class<?>) t ).isPrimitive();
	}

	static public boolean isArray( Type t )
	{
		return t instanceof Class && ( (Class<?>) t ).isArray();
	}

	static public boolean isInterface( Type t )
	{
		return t instanceof Class && ( (Class<?>) t ).isInterface();
	}

	static public boolean isContainer( Type t )
	{
		if( t instanceof Class ) {
			final Class<?> c = (Class<?>) t;

			return c.isInterface();
		}
		if( t instanceof GenericArrayType ) {
			return true;
		}
		if( t instanceof ParameterizedType ) {
			t = ( (ParameterizedType) t ).getRawType();

			return t == Map.class || t == List.class || t == Set.class;
		}

		throw new IllegalArgumentException( format( "Cannot handle %s", t ) );
	}

	private Utils()
	{
	}
}
