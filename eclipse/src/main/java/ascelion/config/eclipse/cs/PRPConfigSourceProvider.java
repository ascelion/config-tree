
package ascelion.config.eclipse.cs;

import java.net.URL;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class PRPConfigSourceProvider extends URLConfigSourceProvider
{

	public PRPConfigSourceProvider()
	{
		super( "META-INF/microprofile-config.properties", false );
	}

	public PRPConfigSourceProvider( String resource )
	{
		super( resource, true );
	}

	@Override
	protected ConfigSource create( URL resource )
	{
		return new PRPConfigSource( resource );
	}

}
