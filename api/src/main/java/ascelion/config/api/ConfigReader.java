
package ascelion.config.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.list;

public interface ConfigReader
{

	@Retention( RUNTIME )
	@Target( TYPE )
	@Qualifier
	@Singleton
	@interface Type
	{

		String value();

		@Nonbinding
		String[] types() default {};
	}

	static List<URL> getResources( String source )
	{
		final List<URL> keys = new ArrayList<>();
		final File file = new File( source );

		try {
			keys.addAll( list( Thread.currentThread().getContextClassLoader().getResources( source ) ) );

			if( file.exists() ) {
				keys.add( file.toURI().toURL() );
			}

			return keys;
		}
		catch( final IOException e ) {
			throw new ConfigException( source, e );
		}
	}

	default boolean enabled()
	{
		return true;
	}

	default Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys ) throws ConfigException
	{
		throw new UnsupportedOperationException( source.value() );
	}

	default Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys, URL url ) throws ConfigException
	{
		try( InputStream is = url.openStream() ) {
			return readConfiguration( source, keys, is );
		}
		catch( final IOException e ) {
			throw new ConfigException( url.toExternalForm(), e );
		}
	}

	default Map<String, ?> readConfiguration( ConfigSource source, Set<String> keys, InputStream is ) throws IOException
	{
		throw new UnsupportedOperationException( "not implemented" );
	}
}
