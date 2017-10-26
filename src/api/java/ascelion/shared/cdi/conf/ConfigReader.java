
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface ConfigReader
{

	default void readConfiguration( ConfigNode root, String source ) throws ConfigException
	{
		throw new UnsupportedOperationException( source );
	}

	default void readConfiguration( ConfigNode root, URL source ) throws ConfigException
	{
		try( InputStream is = source.openStream() ) {
			readConfiguration( root, is );
		}
		catch( final IOException e ) {
			throw new ConfigException( source.toExternalForm(), e );
		}
	}

	default void readConfiguration( ConfigNode root, InputStream source ) throws IOException
	{
		throw new UnsupportedOperationException( "not implemented" );
	}
}
