
package ascelion.cdi.conf;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

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

	private Utils()
	{
	}
}
