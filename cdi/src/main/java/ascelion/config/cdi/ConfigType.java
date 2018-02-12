
package ascelion.config.cdi;

import java.beans.Introspector;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Inject;

import ascelion.cdi.type.AnnotatedTypeW;
import ascelion.config.api.ConfigPrefix;
import ascelion.config.api.ConfigValue;
import ascelion.config.utils.ConfigValueLiteral;

import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Typed
final class ConfigType<X> extends AnnotatedTypeW<X>
{

	static private final Pattern SHORTCUT = Pattern.compile( "^([^${}:]*)(:(.+)$)?" );

	private boolean modified;

	private final Set<ConfigValue> values = new HashSet<>();

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
				e.getParameters()
					.forEach( p -> {
						updateAnnotation( p );
					} );

				addInject( e );
			} );
	}

	private void addInject( AnnotatedCallable<?> e )
	{
		if( e.isAnnotationPresent( Produces.class ) ) {
			return;
		}
		for( final AnnotatedParameter<?> p : e.getParameters() ) {
			if( p.isAnnotationPresent( Observes.class ) ) {
				return;
			}
			if( p.isAnnotationPresent( Disposes.class ) ) {
				return;
			}
		}
		if( !e.isAnnotationPresent( Inject.class ) ) {
			e.getAnnotations().add( InjectLiteral.INSTANCE );

			this.modified = true;
		}
	}

	private void addInject( AnnotatedField<?> e )
	{
		if( !e.isAnnotationPresent( Inject.class ) ) {
			e.getAnnotations().add( InjectLiteral.INSTANCE );

			this.modified = true;
		}
	}

	private void updateAnnotation( AnnotatedParameter<? super X> am )
	{
		final ConfigValue cv = am.getAnnotation( ConfigValue.class );

		if( cv == null ) {
			return;
		}

		String name = null;

		if( cv.value().isEmpty() ) {
			if( am.getJavaParameter().isNamePresent() ) {
				name = am.getJavaParameter().getName();
			}
			else {
				final AnnotatedCallable<? super X> dc = am.getDeclaringCallable();

				if( dc.getParameters().size() == 1 ) {
					name = dc.getJavaMember().getName();

					if( name.startsWith( "set" ) ) {
						name = Introspector.decapitalize( name.substring( 3 ) );
					}
				}
			}
		}

		updateAnnotation( am, cv, name );
	}

	private <X> void updateAnnotation( AnnotatedField<? super X> am, String name )
	{
		final ConfigValue cv = am.getAnnotation( ConfigValue.class );

		if( cv == null ) {
			return;
		}

		updateAnnotation( am, cv, name );
	}

	private void updateAnnotation( Annotated am, ConfigValue cv, String name )
	{
		final ConfigPrefix cp = getAnnotation( ConfigPrefix.class );
		boolean transform = false;
		String val = cv.value();
		String def = null;

		final Matcher mat = SHORTCUT.matcher( val );

		if( mat.matches() ) {
			val = mat.group( 1 );
			def = trimToEmpty( mat.group( 2 ) );

			transform = true;
		}

		if( val.isEmpty() ) {
			val = name;

			transform = true;
		}
		if( cp != null && cp.value().length() > 0 ) {
			val = cp.value() + "." + val;

			transform = true;
		}
		if( transform ) {
			am.getAnnotations().remove( cv );

			if( mat.matches() ) {
				val = "${" + val + def + "}";
			}

			cv = new ConfigValueLiteral( val, cv.converter(), cv.unwrap() );

			am.getAnnotations().add( cv );

			this.modified = true;
		}

		this.values.add( cv );
	}

	boolean modified()
	{
		return this.modified;
	}

	Set<ConfigValue> values()
	{
		return unmodifiableSet( this.values );
	}
}
