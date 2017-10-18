
package ascelion.shared.cdi.conf;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Inject;

import static java.util.Arrays.asList;

import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigExtension implements Extension
{

	static private final Logger L = LoggerFactory.getLogger( ConfigExtension.class );

	private final Set<ConfigSource> sources = new TreeSet<>( ( o1, o2 ) -> {
		if( o1.priority() != o2.priority() ) {
			return Integer.compare( o1.priority(), o2.priority() );
		}

		return o1.value().compareTo( o2.value() );
	} );

	private final Set<Type> types = new HashSet<>();
	private final Set<Type> producedTypes = new HashSet<>();

	private Bean<ConfigProd> producer;

	<X> void collectConfigSourceList( @Observes ProcessAnnotatedType<X> event )
	{
		final AnnotatedType<X> t = event.getAnnotatedType();
		final ConfigSource a1 = t.getAnnotation( ConfigSource.class );
		final ConfigSource.List a2 = t.getAnnotation( ConfigSource.List.class );

		L.debug( "Type: {}", t.getBaseType() );

		if( a1 != null ) {
			this.sources.add( a1 );
		}
		if( a2 != null ) {
			this.sources.addAll( asList( a2.value() ) );
		}

		t.getAnnotations().stream()
			.filter( x -> x.annotationType().isAnnotationPresent( ConfigSource.List.class ) )
			.forEach( x -> this.sources.addAll( asList( x.annotationType().getAnnotation( ConfigSource.List.class ).value() ) ) );
		t.getAnnotations().stream()
			.filter( x -> x.annotationType().isAnnotationPresent( ConfigSource.class ) )
			.forEach( x -> this.sources.add( x.annotationType().getAnnotation( ConfigSource.class ) ) );
	}

	<X> void collectConfigValue( BeanManager bm, @Observes @WithAnnotations( ConfigValue.class ) ProcessAnnotatedType<X> event )
	{
		final ConfigType<X> type = new ConfigType<>( event.getAnnotatedType() );

		if( type.modified() ) {
			L.info( "Found: {}", type.getBaseType().getTypeName() );

			if( L.isDebugEnabled() ) {
				logMembers( "Constructor", type.getConstructors() );
				logMembers( "Field", type.getFields() );
				logMembers( "Method", type.getMethods() );
			}

			event.setAnnotatedType( type );
		}
	}

	void afterTypeDiscovery( BeanManager bm, @Observes AfterTypeDiscovery event )
	{
		addAnnotatedType( bm, event, ConfigProd.class );
	}

	<T, X> void processInjectionPoint( @Observes ProcessInjectionPoint<T, X> event )
	{
		final InjectionPoint p = event.getInjectionPoint();

		if( p.getAnnotated().isAnnotationPresent( ConfigValue.class ) ) {
			L.info( "Config type: {} from {}", p.getType(), p );

			this.types.add( wrap( p.getType() ) );
		}
	}

	void defaultProducer( @Observes ProcessBean<ConfigProd> event )
	{
		L.info( "Default producer: {}", event.getBean() );

		this.producer = event.getBean();
	}

	<T, X> void processProducer( @Observes ProcessProducer<T, X> event )
	{
		final AnnotatedMember<T> m = event.getAnnotatedMember();

		if( m.isAnnotationPresent( ConfigValue.class ) && ( m.getBaseType() != Object.class ) ) {
			final Type t = wrap( m.getBaseType() );

			L.info( "Remove type: {} from {}", t, m );

			this.producedTypes.add( t );
		}
	}

	void afterBeanDiscovery( BeanManager bm, @Observes AfterBeanDiscovery event )
	{
		this.types.removeAll( this.producedTypes );

		if( this.types.size() > 0 ) {
			L.info( "Add producer for types {}", this.types );

			event.addBean( new ConfigProdBean<>( this.producer, this.types ) );
		}
	}

	Set<ConfigSource> sources()
	{
		return this.sources;
	}

	Type wrap( Type t )
	{
		if( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
			t = Primitives.wrap( (Class<?>) t );
		}

		return t;
	}

	private void addAnnotatedType( BeanManager bm, AfterTypeDiscovery event, Class<?> cls )
	{
		event.addAnnotatedType( bm.createAnnotatedType( cls ), cls.getName() );
	}

	private <A extends AnnotatedMember<? super X>, X> void logMembers( String prefix, Collection<A> members )
	{
		members.stream().filter( e -> e.isAnnotationPresent( Inject.class ) ).forEach( e -> L.debug( "{}: {}/{}", prefix, e, e.getBaseType() ) );
	}
}
