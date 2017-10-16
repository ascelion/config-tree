
package ascelion.shared.cdi.conf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.Annotated;

import static java.lang.String.format;

abstract class AnnotatedW implements Annotated
{

	private final Annotated delegate;
	private final Set<Annotation> annotations;

	AnnotatedW( Annotated delegate )
	{
		this.delegate = delegate;
		this.annotations = new LinkedHashSet<>( delegate.getAnnotations() );
	}

	@Override
	public final Type getBaseType()
	{
		return this.delegate.getBaseType();
	}

	@Override
	public final Set<Type> getTypeClosure()
	{
		return this.delegate.getTypeClosure();
	}

	@Override
	public final <A extends Annotation> A getAnnotation( Class<A> annotationType )
	{
		return this.annotations.stream()
			.filter( x -> x.annotationType() == annotationType )
			.map( annotationType::cast )
			.findFirst()
			.orElse( null );
	}

	@Override
	public final boolean isAnnotationPresent( Class<? extends Annotation> annotationType )
	{
		return this.annotations.stream().anyMatch( a -> a.annotationType() == annotationType );
	}

	@Override
	public final Set<Annotation> getAnnotations()
	{
		return this.annotations;
	}

	@Override
	public final String toString()
	{
		final String a = this.annotations.stream()
			.map( e -> "@" + e.annotationType().getSimpleName() )
			.collect( Collectors.joining( " " ) );

		return format( "[W] %s %s", a, name() );
	}

	abstract String name();
}
