
package ascelion.shared.cdi.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public interface ConfigReader
{

	default Map<String, ?> readConfiguration( String source ) throws IOException
	{
		throw new UnsupportedOperationException( source );
	}

	default Map<String, ?> readConfiguration( URL source ) throws IOException
	{
		try( InputStream is = source.openStream() ) {
			return readConfiguration( is );
		}
	}

	default Map<String, ?> readConfiguration( InputStream source ) throws IOException
	{
		throw new UnsupportedOperationException( "not implemented" );
	}

	default void readConfiguration( ConfigNode root, String source ) throws IOException
	{
		throw new UnsupportedOperationException( source );
	}

	default void readConfiguration( ConfigNode root, URL source ) throws IOException
	{
		try( InputStream is = source.openStream() ) {
			readConfiguration( root, is );
		}
	}

	default void readConfiguration( ConfigNode root, InputStream source ) throws IOException
	{
		throw new UnsupportedOperationException( "not implemented" );
	}
}
