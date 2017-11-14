
package ascelion.config.impl;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Inject;

import ascelion.cdi.type.AnnotatedTypeW;
import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigPrefix;
import ascelion.config.api.ConfigValue;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Typed
final class ConfigType<X> extends AnnotatedTypeW<X>
{

	private boolean modified;

	private final Set<Class<? extends ConfigConverter<?>>> converters = new TreeSet<>( new TypeCMP<>() );

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

	private void updateAnnotation( Annotated m, String name )
	{
		ConfigValue a = m.getAnnotation( ConfigValue.class );
		final ConfigPrefix p = getAnnotation( ConfigPrefix.class );

		if( a != null ) {
			boolean transform = false;
			final String[] n = a.value().split( Eval.Token.S_DEF );
			final Class<? extends ConfigConverter<?>> c = (Class<? extends ConfigConverter<?>>) a.converter();

			if( n[0].isEmpty() ) {
				if( isBlank( name ) ) {
					throw new IllegalArgumentException( format( "Need to specify configuration name for %s", m ) );
				}

				n[0] = name;
				transform = true;
			}
			if( p != null ) {
				n[0] = Utils.path( p.value(), n[0] );

				transform = true;
			}
			if( transform ) {
				m.getAnnotations().remove( a );

				a = new ConfigValueLiteral( n, a.converter(), a.unwrap() );

				m.getAnnotations().add( a );

				this.modified = true;
			}

			if( c != ConfigConverter.class ) {
				this.converters.add( c );
			}
		}
	}

	boolean modified()
	{
		return this.modified;
	}

	Collection<Class<? extends ConfigConverter<?>>> converters()
	{
		return unmodifiableSet( this.converters );
	}
}
