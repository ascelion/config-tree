
package ascelion.config.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

@ConfigSource( value = "META-INF/microprofile-config.properties", priority = 100 )
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

	private final Set<ConfigSource> sources = new LinkedHashSet<>();
	private final Map<ConfigValue, Set<Type>> values = new LinkedHashMap<>();
	private final Set<Class<ConfigReader>> readers = new LinkedHashSet<>();
	private final FastClasspathScanner cs;
	private final List<Predicate<Class<?>>> filters = new ArrayList<>();

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

	public Map<ConfigValue, Set<Type>> getValues()
	{
		scan();

		return unmodifiableMap( this.values );
	}

	public Collection<Class<ConfigReader>> getReaders()
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
		if( this.sources.isEmpty() && this.values.isEmpty() ) {
			this.cs.scan()
				.getNamesOfAllStandardClasses()
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
		processAnnotation( ConfigReader.Type.class, cls, a -> this.readers.add( (Class<ConfigReader>) cls ) );

		Stream.of( cls.getDeclaredFields() )
			.forEach( f -> processAnnotation( ConfigValue.class, f, a -> addConfigValue( a, f.getGenericType() ) ) );
		Stream.of( cls.getDeclaredConstructors() )
			.forEach( c -> Stream.of( c.getParameters() ).forEach( p -> processAnnotation( ConfigValue.class, p, a -> addConfigValue( a, p.getParameterizedType() ) ) ) );
		Stream.of( cls.getDeclaredMethods() )
			.forEach( c -> Stream.of( c.getParameters() ).forEach( p -> processAnnotation( ConfigValue.class, p, a -> addConfigValue( a, p.getParameterizedType() ) ) ) );
	}

	private void addConfigValue( ConfigValue a, Type genericType )
	{
		this.values.computeIfAbsent( a, k -> new LinkedHashSet<>() ).add( genericType );
	}

	private <A extends Annotation> void processAnnotation( Class<A> annotation, AnnotatedElement element, Consumer<A> action )
	{
		try {
			final A a = element.getAnnotation( annotation );

			if( a != null ) {
				action.accept( a );
			}
		}
		catch( final Exception e ) {
			;
		}
	}

}
