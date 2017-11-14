
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Inject;

import ascelion.cdi.type.AnnotatedTypeW;
import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.config.impl.read.INIConfigReader;
import ascelion.config.impl.read.JMXConfigReader;
import ascelion.config.impl.read.PRPConfigReader;
import ascelion.config.impl.read.XMLConfigReader;
import ascelion.config.impl.read.YMLConfigReader;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import com.google.common.primitives.Primitives;
import org.apache.deltaspike.core.api.literal.SingletonLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigExtension implements Extension
{

	static private final Logger L = LoggerFactory.getLogger( ConfigExtension.class );

	private final Set<ConfigSource> sources = new HashSet<>();
	private final Set<Class<? extends ConfigConverter<?>>> converters = new TreeSet<>( new TypeCMP<>() );
	private final Set<Type> types = new TreeSet<>( new TypeCMP<>() );
	private final Set<Type> producedTypes = new TreeSet<>( new TypeCMP<>() );
	private Bean<ConfigProd> producer;

	void beforeBeanDiscovery( BeanManager bm, @Observes BeforeBeanDiscovery event )
	{
		addType( Converters.class, bm, event );
		addType( INIConfigReader.class, bm, event );
		addType( JMXConfigReader.class, bm, event );
		addType( PRPConfigReader.class, bm, event );
		addType( XMLConfigReader.class, bm, event );
		addType( YMLConfigReader.class, bm, event );
	}

	private void addType( Class<?> type, BeanManager bm, BeforeBeanDiscovery event )
	{
		AnnotatedType<?> at = bm.createAnnotatedType( type );

		at = new AnnotatedTypeW<>( at );

		at.getAnnotations().add( new SingletonLiteral() );

		event.addAnnotatedType( at );
	}

	<X> void collectConfigSourceList( @Observes ProcessAnnotatedType<X> event )
	{
		final AnnotatedType<X> t = event.getAnnotatedType();
		final ConfigSource a1 = t.getAnnotation( ConfigSource.class );
		final ConfigSource.List a2 = t.getAnnotation( ConfigSource.List.class );

		if( a1 != null ) {
			addSources( t, a1 );
		}
		if( a2 != null ) {
			addSources( t, a2.value() );
		}

		t.getAnnotations().stream()
			.filter( x -> x.annotationType().isAnnotationPresent( ConfigSource.List.class ) )
			.forEach( x -> addSources( t, x.annotationType().getAnnotation( ConfigSource.List.class ).value() ) );
		t.getAnnotations().stream()
			.filter( x -> x.annotationType().isAnnotationPresent( ConfigSource.class ) )
			.forEach( x -> addSources( t, x.annotationType().getAnnotation( ConfigSource.class ) ) );
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

			this.converters.addAll( type.converters() );
		}
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
		if( event.getAnnotated() instanceof AnnotatedMember ) {
			L.info( "Default producer: {}", event.getBean() );

			this.producer = event.getBean();
		}
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

	Collection<ConfigSource> sources()
	{
		return unmodifiableSet( this.sources );
	}

	Collection<Class<? extends ConfigConverter<?>>> converters()
	{
		return unmodifiableSet( this.converters );
	}

	Type wrap( Type t )
	{
		if( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
			t = Primitives.wrap( (Class<?>) t );
		}

		return t;
	}

	private void addSources( AnnotatedType<?> t, ConfigSource... sources )
	{
		final List<ConfigSource> all = asList( sources );

		L.info( "Source: {}", t.getBaseType() );
		L.debug( "\t{}", all );

		this.sources.addAll( all );
	}

	private <A extends AnnotatedMember<? super X>, X> void logMembers( String prefix, Collection<A> members )
	{
		members.stream().filter( e -> e.isAnnotationPresent( Inject.class ) ).forEach( e -> L.debug( "\t{}: {}/{}", prefix, e, e.getBaseType() ) );
	}
}
