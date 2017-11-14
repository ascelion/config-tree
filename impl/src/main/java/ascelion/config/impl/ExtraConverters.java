
package ascelion.config.impl;

import java.net.MalformedURLException;
import java.net.URL;

import ascelion.config.api.ConfigException;

import static ascelion.config.impl.Utils.toStream;

class ExtraConverters
{

	static Class<?> createClass( String x )
	{
		try {
			return Thread.currentThread().getContextClassLoader().loadClass( x );
		}
		catch( final ClassNotFoundException e ) {
			throw new ConfigException( x, e );
		}
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
		return toStream( u )
			.map( Integer::parseInt )
			.mapToInt( Integer::intValue )
			.toArray();
	}

	static long[] createLongA( String u )
	{
		return toStream( u )
			.map( Long::parseLong )
			.mapToLong( Long::longValue )
			.toArray();
	}

	static double[] createDoubleA( String u )
	{
		return toStream( u )
			.map( Double::parseDouble )
			.mapToDouble( Double::doubleValue )
			.toArray();
	}
}
