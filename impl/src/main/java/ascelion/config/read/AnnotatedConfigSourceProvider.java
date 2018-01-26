
package ascelion.config.read;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.eclipse.References;
import ascelion.config.impl.ConfigSources;
import ascelion.logging.LOG;

import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public class AnnotatedConfigSourceProvider implements ConfigSourceProvider
{

	static private List<URL> getResources( String source, ClassLoader cld )
	{
		try {
			return singletonList( new URL( source ) );
		}
		catch( final MalformedURLException e ) {
			;
		}

		final List<URL> keys = new ArrayList<>();

		try {
			keys.addAll( list( cld.getResources( source ) ) );

			final File file = new File( source );

			if( file.exists() ) {
				keys.add( file.toURI().toURL() );
			}

			return keys;
		}
		catch( final IOException e ) {
			throw new ConfigException( source, e );
		}
	}

	static private String getType( ascelion.config.api.ConfigSource source )
	{
		String t = source.type();

		if( isBlank( t ) ) {
			t = FilenameUtils.getExtension( source.value() );

			if( isBlank( t ) ) {
				t = source.value();
			}
		}

		return t.toUpperCase();
	}

	static private final LOG L = LOG.get();
	private final References<Iterable<ConfigSource>> sources = new References<>();

	@Override
	public Iterable<ConfigSource> getConfigSources( ClassLoader cld )
	{
		return this.sources.get( cld, this::buildSources );
	}

	private Iterable<ConfigSource> buildSources( ClassLoader cld )
	{
		final Collection<ConfigSource> built = new ArrayList<>();
		final Map<String, ConfigReader> readers = new TreeMap<>();

		ConfigSources.instance().getReaders( cld )
			.forEach( rd -> {
				for( final String t : rd.types() ) {
					readers.put( t.toUpperCase(), rd );
				}
			} );
		ConfigSources.instance()
			.getSources( cld )
			.forEach( cs -> {
				final String st = getType( cs );
				final ConfigReader rd = readers.get( st );

				if( rd != null ) {
					if( cs.value().length() > 0 ) {
						AnnotatedConfigSourceProvider.getResources( cs.value(), cld )
							.forEach( u -> {
								built.add( new AnnotatedConfigSource( rd, u.toExternalForm(), cs.priority() ) );
							} );
					}
					else {
						built.add( new AnnotatedConfigSource( rd, cs.value(), cs.priority() ) );
					}
				}
				else {

				}
			} );

		return built;
	}

}
