
package ascelion.shared.cdi.conf;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Inject;

import com.google.common.primitives.Primitives;

@Typed
final class ConfigType<X> extends AnnotatedTypeW<X>
{

	private final Set<Type> types = new HashSet<>();

	private boolean modified;

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
				collectType( e );
				addConv( e );
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
					collectType( p );
					addConv( p );
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

	private void addConv( Annotated p )
	{
		final ConfigValue a = p.getAnnotation( ConfigValue.class );

		if( a != null && a.converter() == BiFunction.class ) {
			p.getAnnotations().remove( a );
			p.getAnnotations().add( new ConfigValue.Literal( a, DefaultCVT.class ) );

			this.modified = true;
		}
	}

	private void collectType( Annotated m )
	{
		Type t = m.getBaseType();

		if( t instanceof Class ) {
			t = Primitives.wrap( (Class<?>) t );
		}

		this.types.add( t );
	}

	boolean modified()
	{
		return this.modified;
	}

	Collection<? extends Type> types()
	{
		return this.types;
	}
}
