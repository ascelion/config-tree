
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigSource;
import ascelion.config.eclipse.ext.ConfigExt;
import ascelion.config.impl.ConfigNodeImpl.ConfigNodeTA;
import ascelion.logging.LOG;

import static java.util.Arrays.asList;

import com.google.gson.GsonBuilder;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public class DefaultConfigRegistry extends ConfigRegistry
{

	static private final LOG L = LOG.get();

	static private final String[] SCAN_PACKAGES = {
		"-java",
		"-javax",
		"-sun",
		"-ibm",
		"-com.sun",
		"-com.ibm",
		"-org.antlr",
		"-org.yaml",
		"-org.stringtemplate",
		"-org.junit",
		"-org.hamcrest",
		"-org.slf4j",
		"-org.ini4j",
	};

	@Override
	protected Iterable<ConfigSource> loadSources( ClassLoader cld )
	{
		return () -> {
			final Set<ConfigSource> sources = new HashSet<>();
			final FastClasspathScanner fcs = new FastClasspathScanner( SCAN_PACKAGES );

			fcs.addClassLoader( cld );
			fcs.matchClassesWithAnnotation( ConfigSource.class, c -> {
				sources.add( c.getAnnotation( ConfigSource.class ) );
			} );
			fcs.matchClassesWithAnnotation( ConfigSource.List.class, c -> {
				sources.addAll( asList( c.getAnnotationsByType( ConfigSource.class ) ) );
			} );
			fcs.scan();

			return sources.iterator();
		};
	}

	@Override
	protected Iterable<ConfigReader> loadReaders( ClassLoader cld )
	{
		return ServiceLoader.load( ConfigReader.class, cld );
	}

	@Override
	protected final ConfigNode load( ClassLoader cld )
	{
		final Config config = ConfigProviderResolver.instance().getConfig( cld );
		final ConfigNodeImpl root = new ConfigNodeImpl();

		readConfig( root, config );

		if( config instanceof ConfigExt ) {
			( (ConfigExt) config ).addChangeListener( cs -> root.setValue( cs.getProperties() ) );
		}

		return root;
	}

	private void readConfig( ConfigNodeImpl root, Config config )
	{
		final List<org.eclipse.microprofile.config.spi.ConfigSource> sources = new ArrayList<>();

		config.getConfigSources().forEach( sources::add );

		Collections.reverse( sources );

		sources.forEach( cs -> {
			L.debug( "Reading:%s", cs );

			root.setValue( cs.getProperties() );
		} );

		if( L.isTraceEnabled() ) {
			final String s = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeHierarchyAdapter( ConfigNode.class, new ConfigNodeTA() )
				.create()
				.toJson( root );

			L.trace( "Config: %s", s );
		}
	}
}
