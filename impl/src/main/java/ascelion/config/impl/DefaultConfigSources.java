
package ascelion.config.impl;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static java.util.Arrays.asList;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public class DefaultConfigSources extends ConfigSources
{

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
}
