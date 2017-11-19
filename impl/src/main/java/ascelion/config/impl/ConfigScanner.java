
package ascelion.config.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static java.util.Collections.unmodifiableSet;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public class ConfigScanner
{

	static private final String[] PACKAGES = {
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

	private final FastClasspathScanner cs;
	private final List<Predicate<Class<?>>> filters = new ArrayList<>();
	private final Set<ConfigSource> sources = new LinkedHashSet<>();
	private final Set<Class<? extends ConfigReader>> readers = new LinkedHashSet<>();

	public ConfigScanner()
	{
		this( false );
	}

	public ConfigScanner( boolean verbose )
	{
		this.cs = new FastClasspathScanner( PACKAGES );

		this.cs.verbose( verbose );

		addFilter( c -> !c.isAnonymousClass() );

		Stream.of( this.cs.findBestClassLoader() )
			.forEach( this.cs::addClassLoader );
	}

	public Set<ConfigSource> getSources()
	{
		scan();

		return unmodifiableSet( this.sources );
	}

	public Set<Class<? extends ConfigReader>> getReaders()
	{
		scan();

		return unmodifiableSet( this.readers );
	}

	public void addFilter( Predicate<Class<?>> filter )
	{
		this.filters.add( filter );
	}

	private void scan()
	{
		this.cs.scan()
			.getNamesOfAllClasses()
			.forEach( n -> {
				try {
					processClass( Thread.currentThread().getContextClassLoader().loadClass( n ) );
				}
				catch( final ClassNotFoundException e ) {
				}
				catch( final NoClassDefFoundError e ) {
				}
			} );
	}

	private void processClass( Class<?> cls )
	{
		if( this.filters.size() > 0 ) {
			boolean reject = true;

			for( final Predicate<Class<?>> f : this.filters ) {
				if( f.test( cls ) ) {
					reject = false;

					break;
				}
			}

			if( reject ) {
				return;
			}
		}

		processAnnotation( ConfigSource.class, cls, ( a, c ) -> this.sources.add( a ) );
		processAnnotation( ConfigReader.Type.class, cls, ( a, c ) -> this.readers.add( (Class<? extends ConfigReader>) c ) );
	}

	private <A extends Annotation> void processAnnotation( Class<A> type, Class<?> cls, BiConsumer<A, Class<?>> action )
	{
		try {
			Stream.of( cls.getAnnotationsByType( type ) ).forEach( a -> action.accept( a, cls ) );
		}
		catch( final Exception e ) {
			;
		}
	}

}
