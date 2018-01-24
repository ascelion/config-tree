
package ascelion.config.eclipse.cs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.list;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public abstract class URLConfigSourceProvider implements ConfigSourceProvider
{

	private final String resource;
	private final boolean checkFile;

	URLConfigSourceProvider( String resource, boolean checkFile )
	{
		this.resource = resource;
		this.checkFile = checkFile;
	}

	@Override
	public final Iterable<ConfigSource> getConfigSources( ClassLoader cld )
	{
		return () -> getResources( cld ).stream().map( this::create ).iterator();
	}

	protected abstract ConfigSource create( URL resource );

	private List<URL> getResources( ClassLoader cld )
	{
		final List<URL> urls = new ArrayList<>();

		try {
			urls.addAll( list( cld.getResources( this.resource ) ) );

			if( this.checkFile ) {
				final File file = new File( this.resource );

				if( file.exists() ) {
					urls.add( file.toURI().toURL() );
				}
			}

			return urls;
		}
		catch( final IOException e ) {
			e.printStackTrace();

			return emptyList();
		}
	}
}
