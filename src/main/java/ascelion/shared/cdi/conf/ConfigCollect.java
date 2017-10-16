
package ascelion.shared.cdi.conf;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.io.FilenameUtils;

@ApplicationScoped
class ConfigCollect
{

	@Inject
	private BeanManager bm;

	@Inject
	private ConfigExtension ext;

	private final ConfigMap cm = new ConfigMap();

	ConfigMap cm()
	{
		return this.cm;
	}

	@PostConstruct
	private void postConstruct()
	{

		this.ext.sources().forEach( s -> {
			final String f = s.value();
			final String t = getType( s );

			final Bean<ConfigReader> rdb = getReader( t );
			final CreationalContext<ConfigReader> cc = this.bm.createCreationalContext( rdb );
			final ConfigReader rd = (ConfigReader) this.bm.getReference( rdb, ConfigReader.class, cc );

			for( final Enumeration<URL> e = getURL( f ); e.hasMoreElements(); ) {
				final URL u = e.nextElement();

				try {
					this.cm.add( rd.readConfiguration( u ) );
				}
				catch( final IOException x ) {
					throw new RuntimeException( u.toExternalForm(), x );
				}
			}
		} );

		System.getProperties().forEach( ( k, v ) -> this.cm.setValue( (String) k, (String) v ) );
	}

	private Bean<ConfigReader> getReader( String t )
	{
		return (Bean<ConfigReader>) this.bm.getBeans( ConfigReader.class, new AnyLiteral() ).stream()
			.filter( b -> b.getBeanClass().isAnnotationPresent( ConfigSource.Type.class ) )
			.filter( b -> matches( b.getBeanClass().getAnnotation( ConfigSource.Type.class ), t ) )
			.findFirst()
			.orElseThrow( () -> new UnsatisfiedResolutionException() );
	}

	private boolean matches( ConfigSource.Type t1, String t )
	{
		return Stream.concat( Stream.of( t1.value() ), Stream.of( t1.types() ) )
			.anyMatch( x -> t.equalsIgnoreCase( x ) );
	}

	private String getType( final ConfigSource s )
	{
		String t = s.type().value();

		if( isBlank( t ) ) {
			t = FilenameUtils.getExtension( s.value() );

			if( isBlank( t ) ) {
				throw new RuntimeException( format( "No type specified for configuration source %s", s.value() ) );
			}
		}

		return t;
	}

	private Enumeration<URL> getURL( String source )
	{
		final File file = new File( source );

		try {
			if( file.exists() ) {
				return enumeration( asList( file.toURI().toURL() ) );
			}

			return Thread.currentThread().getContextClassLoader().getResources( source );
		}
		catch( final IOException e ) {
			throw new RuntimeException( source, e );
		}
	}
}
