
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
import ascelion.config.api.ConfigRegistry;
import ascelion.config.utils.References;
import ascelion.logging.LOG;

import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

public class AnnotatedConfigSourceProvider implements ConfigSourceProvider
{

	static private final LOG L = LOG.get();

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
			final File file = new File( source );

			if( file.exists() ) {
				keys.add( file.toURI().toURL() );
			}

			keys.addAll( list( cld.getResources( source ) ) );

			return keys;
		}
		catch( final IOException e ) {
			throw new ConfigException( source, e );
		}
	}

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
		final ConfigRegistry reg = ConfigRegistry.getInstance( cld );

		reg.getReaders()
			.forEach( rd -> {
				L.trace( "Found reader: %s -> %s", rd.getClass().getName(), rd.types() );

				for( final String t : rd.types() ) {
					readers.put( t.toUpperCase(), rd );
				}
			} );
		reg.getSources()
			.forEach( cs -> {
				final String st = getType( cs );
				final ConfigReader rd = readers.get( st );

				if( rd != null ) {
					if( cs.value().length() > 0 ) {
						final List<URL> resources = getResources( cs.value(), cld );

						if( resources.isEmpty() ) {
							L.trace( "Found source: %s(%d) -> %s", st, cs.priority(), cs.value() );

							built.add( new AnnotatedConfigSource( rd, st, cs.value(), cs.priority() ) );
						}
						else {
							resources.forEach( u -> {
								L.trace( "Found source: %s(%d) -> %s", st, cs.priority(), u );

								built.add( new AnnotatedConfigSource( rd, st, u.toExternalForm(), cs.priority() ) );
							} );
						}
					}
					else {
						L.trace( "Found source: %s(%d)", st, cs.priority() );

						built.add( new AnnotatedConfigSource( rd, st, cs.value(), cs.priority() ) );
					}
				}
				else {
					L.warn( "No reader for %s(%d) -> %s", st, cs.priority(), cs.value() );
				}
			} );

		return built;
	}

}
