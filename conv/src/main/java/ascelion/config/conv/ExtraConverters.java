
package ascelion.config.conv;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;

import static ascelion.config.conv.Utils.values;

class ExtraConverters
{

	static private final Map<String, Boolean> BOOLEAN_VALUES = new HashMap<>();

	static {
		BOOLEAN_VALUES.put( "true", true );
		BOOLEAN_VALUES.put( "1", true );
		BOOLEAN_VALUES.put( "y", true );
		BOOLEAN_VALUES.put( "yes", true );
		BOOLEAN_VALUES.put( "on", true );

		try {
			final ResourceBundle b = ResourceBundle.getBundle( "META-INF/boolean.properties" );

			for( final Enumeration<String> en = b.getKeys(); en.hasMoreElements(); ) {
				final String k = en.nextElement();

				BOOLEAN_VALUES.put( k, Boolean.valueOf( b.getString( k ) ) );
			}
		}
		catch( final MissingResourceException e ) {
			;
		}
	}

	static Class<?> createClass( String x )
	{
		try {
			return Thread.currentThread().getContextClassLoader().loadClass( x );
		}
		catch( final ClassNotFoundException e ) {
			throw new ConfigException( x, e );
		}
	}

	static Boolean createBoolean( String x )
	{
		final Boolean b = BOOLEAN_VALUES.get( x.toLowerCase() );

		if( b == null ) {
			return false;
		}

		return b;
	}

	static URL createURL( String x )
	{
		try {
			return new URL( x );
		}
		catch( final MalformedURLException e ) {
			throw new ConfigException( x, e );
		}
	}

	static int[] createIntA( String u )
	{
		return Stream.of( values( u ) )
			.map( Integer::parseInt )
			.mapToInt( Integer::intValue )
			.toArray();
	}

	static long[] createLongA( String u )
	{
		return Stream.of( values( u ) )
			.map( Long::parseLong )
			.mapToLong( Long::longValue )
			.toArray();
	}

	static double[] createDoubleA( String u )
	{
		return Stream.of( values( u ) )
			.map( Double::parseDouble )
			.mapToDouble( Double::doubleValue )
			.toArray();
	}
}
