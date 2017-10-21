
package ascelion.shared.cdi.conf;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Inject;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Typed
final class ConfigType<X> extends AnnotatedTypeW<X>
{

	private boolean modified;

	private final Set<Class<? extends BiFunction>> converters = new LinkedHashSet<>();

	ConfigType( AnnotatedType<X> delegate )
	{
		super( delegate );

		processCallables( getConstructors() );
		processFields( getFields() );
		processCallables( getMethods() );
	}

	private void processFields( Collection<AnnotatedField<? super X>> set )
	{
		set.stream()
			.filter( e -> !e.isAnnotationPresent( Produces.class ) )
			.filter( e -> e.isAnnotationPresent( ConfigValue.class ) )
			.forEach( e -> {
				updateAnnotation( e, e.getJavaMember().getName() );
				addInject( e );
			} );
	}

	private <A extends AnnotatedCallable<? super X>> void processCallables( Collection<A> set )
	{
		set.stream()
			.filter( e -> !e.isAnnotationPresent( Produces.class ) )
			.filter( e -> e.getParameters().stream().anyMatch( p -> p.isAnnotationPresent( ConfigValue.class ) ) )
			.forEach( e -> {
				e.getParameters().forEach( p -> {
					updateAnnotation( p, null );
				} );
				addInject( e );
			} );
	}

	private void addInject( Annotated e )
	{
		if( !e.isAnnotationPresent( Inject.class ) ) {
			e.getAnnotations().add( new InjectLiteral() );

			this.modified = true;
		}
	}

	private void updateAnnotation( Annotated p, String name )
	{
		final ConfigValue a = p.getAnnotation( ConfigValue.class );

		if( a != null ) {
			boolean transform = false;
			String n = a.value();
			Class<? extends BiFunction> c = a.converter();

			if( n.isEmpty() ) {
				if( isBlank( name ) ) {
					throw new IllegalArgumentException( format( "Need to specify configuration name for %s", p ) );
				}

				n = name;
				transform = true;
			}
			if( c == BiFunction.class ) {
				c = DefaultCVT.class;

				transform = true;
			}
			if( transform ) {
				p.getAnnotations().remove( a );
				p.getAnnotations().add( new ConfigValueLiteral( n, c, a.unwrap() ) );

				this.modified = true;
			}

			this.converters.add( c );
		}
	}

	boolean modified()
	{
		return this.modified;
	}

	Set<Class<? extends BiFunction>> converters()
	{
		return this.converters;
	}
}
