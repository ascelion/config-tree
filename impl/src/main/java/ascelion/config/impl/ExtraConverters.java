
package ascelion.config.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;

import static ascelion.config.impl.Utils.values;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

class ExtraConverters
{

	static private final Map<String, Boolean> BOOLEAN_VALUES = new HashMap<>();

	static {
		BOOLEAN_VALUES.put( "false", false );
		BOOLEAN_VALUES.put( "0", false );
		BOOLEAN_VALUES.put( "n", false );
		BOOLEAN_VALUES.put( "no", false );
		BOOLEAN_VALUES.put( "off", false );

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

	static <T> Function<String, T> wrap( Function<String, T> conv )
	{
		return x -> {
			if( isBlank( x ) ) {
				x = "0";
			}

			return conv.apply( x );
		};
	}

	static Boolean createBoolean( String x )
	{
		final Boolean b = BOOLEAN_VALUES.get( x );

		if( b == null ) {
			throw new IllegalArgumentException( format( "Cannot convert value '%s' to a boolean", x ) );
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
