
package ascelion.config.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

	public void addFilter( Predicate<Class<?>> filter )
	{
		this.filters.add( filter );
	}

	private void scan()
	{
		if( this.sources.isEmpty() ) {
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

		processAnnotation( ConfigSource.class, cls, this.sources::add );
	}

	private <A extends Annotation> void processAnnotation( Class<A> annotation, AnnotatedElement element, Consumer<A> action )
	{
		try {
			Stream.of( element.getAnnotationsByType( annotation ) ).forEach( action );
		}
		catch( final Exception e ) {
			;
		}
	}

}
