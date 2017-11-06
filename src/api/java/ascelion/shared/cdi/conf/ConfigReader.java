
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface ConfigReader
{

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
