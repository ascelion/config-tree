
package ascelion.cdi.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.list;

public interface ConfigReader
{

	static List<URL> getResources( String source )
	{
		final List<URL> all = new ArrayList<>();
		final File file = new File( source );

		try {
			all.addAll( list( Thread.currentThread().getContextClassLoader().getResources( source ) ) );

			if( file.exists() ) {
				all.add( file.toURI().toURL() );
			}

			return all;
		}
		catch( final IOException e ) {
			throw new ConfigException( source, e );
		}
	}

	default boolean enabled()
	{
		return true;
	}

	default void readConfiguration( ConfigSource source, ConfigNode root ) throws ConfigException
	{
		throw new UnsupportedOperationException( source.value() );
	}

	default void readConfiguration( ConfigSource source, ConfigNode root, URL url ) throws ConfigException
	{
		try( InputStream is = url.openStream() ) {
			readConfiguration( source, root, is );
		}
		catch( final IOException e ) {
			throw new ConfigException( url.toExternalForm(), e );
		}
	}

	default void readConfiguration( ConfigSource source, ConfigNode root, InputStream is ) throws IOException
	{
		throw new UnsupportedOperationException( "not implemented" );
	}
}
