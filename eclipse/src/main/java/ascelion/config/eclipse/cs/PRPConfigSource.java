
package ascelion.config.eclipse.cs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

final class PRPConfigSource extends URLConfigSource
{

	PRPConfigSource( URL resource )
	{
		super( resource );
	}

	@Override
	protected Map<String, String> readConfiguration( InputStream is ) throws IOException
	{
		final Properties props = new Properties();

		props.load( is );

		return (Map) props;
	}
}
