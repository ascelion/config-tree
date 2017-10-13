
package ascelion.shared.cdi.conf;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Inject;

import com.google.common.primitives.Primitives;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigExtension implements Extension
{

	static private final Logger L = LoggerFactory.getLogger( ConfigExtension.class );

	private final Set<Type> types = new HashSet<>();

	private Bean<ConfigProd> producer;

	<X> void processAnnotatedTypeConfigValue( BeanManager bm, @Observes @WithAnnotations( ConfigValue.class ) ProcessAnnotatedType<X> event )
	{
		final AnnotatedType<X> type = event.getAnnotatedType();

		final List<AnnotatedConstructor<X>> constructors = collectCallables( type.getConstructors() );
		final List<AnnotatedField<? super X>> fields = collectFields( type.getFields() );
		final List<AnnotatedMethod<? super X>> methods = collectCallables( type.getMethods() );

		if( fields.size() > 0 || constructors.size() > 0 || methods.size() > 0 ) {
			L.info( "Found: {}", type.getBaseType().getTypeName() );

			if( L.isDebugEnabled() ) {
				constructors.forEach( f -> L.debug( "Constructor: {}", f.getJavaMember() ) );
				fields.forEach( f -> L.debug( "Field: {}", f.getJavaMember() ) );
				methods.forEach( f -> L.debug( "Method: {}", f.getJavaMember() ) );
			}

			final AnnotatedTypeBuilder<X> atb = new AnnotatedTypeBuilder<X>()
				.readFromType( type );

			constructors.forEach( m -> {
				atb.addToConstructor( m, AnnotationInstanceProvider.of( Inject.class ) );

				m.getParameters().forEach( p -> {
					addType( p );

					final ConfigValue a = p.getAnnotation( ConfigValue.class );
					if( a != null && a.converter() == BiFunction.class ) {
						atb.removeFromConstructorParameter( m.getJavaMember(), p.getPosition(), ConfigValue.class );
						atb.addToConstructorParameter( m.getJavaMember(), p.getPosition(), cloneAnnotation( a ) );
					}
				} );
			} );
			fields.forEach( m -> {
				addType( m );

				final ConfigValue a = m.getAnnotation( ConfigValue.class );
				if( a.converter() == BiFunction.class ) {
					atb.removeFromField( m, ConfigValue.class );
					atb.addToField( m, cloneAnnotation( a ) );
				}
				atb.addToField( m, AnnotationInstanceProvider.of( Inject.class ) );
			} );
			methods.forEach( m -> {
				atb.addToMethod( m, AnnotationInstanceProvider.of( Inject.class ) );

				m.getParameters().forEach( p -> {
					addType( p );

					final ConfigValue a = p.getAnnotation( ConfigValue.class );
					if( a != null && a.converter() == BiFunction.class ) {
						atb.removeFromMethodParameter( m.getJavaMember(), p.getPosition(), ConfigValue.class );
						atb.addToMethodParameter( m.getJavaMember(), p.getPosition(), cloneAnnotation( a ) );
					}
				} );

			} );

			event.setAnnotatedType( atb.create() );
		}
	}

	void afterTypeDiscovery( BeanManager bm, @Observes AfterTypeDiscovery event )
	{
		addAnnotatedType( bm, event, ConfigProd.class );
		addAnnotatedType( bm, event, DefaultCVT.class );
	}

	void defaultProducer( @Observes ProcessBean<ConfigProd> event )
	{
		this.producer = event.getBean();
	}

	<T, X> void processProducer( @Observes ProcessProducer<T, X> event )
	{
		if( event.getAnnotatedMember().getAnnotation( ConfigValue.class ) != null ) {
			this.types.remove( event.getAnnotatedMember().getBaseType() );
		}
	}

	void afterBeanDiscovery( BeanManager bm, @Observes AfterBeanDiscovery event )
	{
		if( this.types.size() > 0 ) {
			L.info( "Producer: {}", this.types );

			event.addBean( new ConfigBean<>( this.producer, this.types ) );
		}
	}

	private void addType( Annotated m )
	{
		Type t = m.getBaseType();

		if( t instanceof Class ) {
			t = Primitives.wrap( (Class<?>) t );
		}

		this.types.add( t );
	}

	private void addAnnotatedType( BeanManager bm, AfterTypeDiscovery event, Class<?> cls )
	{
		final AnnotatedType<?> at = bm.createAnnotatedType( cls );

		event.addAnnotatedType( at, cls.getName() );
	}

	private <A extends AnnotatedField<? super X>, X> List<A> collectFields( Collection<A> set )
	{
		return set.stream()
			.filter( e -> !e.isAnnotationPresent( Produces.class ) )
			.filter( e -> e.isAnnotationPresent( ConfigValue.class ) )
			.collect( Collectors.toList() );
	}

	private <A extends AnnotatedCallable<? super X>, X> List<A> collectCallables( Collection<A> set )
	{
		return set.stream()
			.filter( e -> !e.isAnnotationPresent( Produces.class ) )
			.filter( e -> e.getParameters().stream().anyMatch( p -> p.isAnnotationPresent( ConfigValue.class ) ) )
			.collect( Collectors.toList() );
	}

	private ConfigValue cloneAnnotation( ConfigValue a )
	{
		final Map<String, Object> m = new HashMap<>();

		m.put( "value", a.value() );
		m.put( "unwrap", a.unwrap() );
		m.put( "converter", DefaultCVT.class );

		return AnnotationInstanceProvider.of( ConfigValue.class, m );
	}

}
