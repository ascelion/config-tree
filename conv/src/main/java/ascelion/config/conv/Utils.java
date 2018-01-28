
package ascelion.config.conv;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class Utils
{

	private static final String ARRAY_SEPARATOR_REGEX = "(?<!\\\\)" + Pattern.quote( "," );

	static public Set<Method> methodsOf( Class<?> cls )
	{
		return Stream.of( cls.getMethods() ).collect( Collectors.toSet() );
	}

	static public String[] values( String value )
	{
		if( isBlank( value ) ) {
			return new String[0];
		}

		final String[] v = value.split( ARRAY_SEPARATOR_REGEX );

		for( int k = 0; k < v.length; k++ ) {
			v[k] = v[k].trim().replace( "\\,", "," ).replace( "\\;", ";" );
		}

		return v;
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
