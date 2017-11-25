
package ascelion.config.cdi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import ascelion.config.impl.ConfigValueLiteral;

import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Typed
final class ConfigType<X> extends AnnotatedTypeW<X>
{

	static private final Pattern SHORTCUT = Pattern.compile( "^([^${}:]*)(:(.+)$)?" );

	private boolean modified;

	private final Set<Class<? extends ConfigConverter<?>>> converters = new TreeSet<>( new TypeCMP<>() );

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

	private void updateAnnotation( Annotated am, String name )
	{
		ConfigValue cv = am.getAnnotation( ConfigValue.class );

		if( cv == null ) {
			return;
		}

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

		final Class<? extends ConfigConverter<?>> c = (Class<? extends ConfigConverter<?>>) cv.converter();

		if( !ConfigConverter.class.equals( c ) ) {
			this.converters.add( c );
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

	Set<ConfigValue> values()
	{
		return unmodifiableSet( this.values );
	}
}