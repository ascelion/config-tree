
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
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Inject;

import static java.util.Arrays.asList;

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

		this.types.addAll( type.types() );

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

	Set<ConfigSource> sources()
	{
		return this.sources;
	}

	private void addAnnotatedType( BeanManager bm, AfterTypeDiscovery event, Class<?> cls )
	{
		event.addAnnotatedType( bm.createAnnotatedType( cls ), cls.getName() );
	}

	private <A extends AnnotatedMember<? super X>, X> void logMembers( String prefix, Collection<A> members )
	{
		members.stream().filter( e -> e.isAnnotationPresent( Inject.class ) ).forEach( e -> L.debug( "{}: {}", prefix, e ) );
	}
}
