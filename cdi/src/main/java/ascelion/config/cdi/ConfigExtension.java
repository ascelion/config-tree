
package ascelion.config.cdi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProducerFactory;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Inject;

import ascelion.cdi.bean.BeanAttributesBuilder;
import ascelion.cdi.bean.BeanBuilder;
import ascelion.cdi.type.AnnotatedTypeW;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.config.read.INIConfigReader;
import ascelion.config.read.JMXConfigReader;
import ascelion.config.read.PRPConfigReader;
import ascelion.config.read.XMLConfigReader;
import ascelion.config.read.YMLConfigReader;
import ascelion.logging.LOG;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import com.google.common.primitives.Primitives;

public class ConfigExtension implements Extension
{

	static private final LOG L = LOG.get();

	private final Set<ConfigSource> sources = new HashSet<>();
	private final Set<ConfigValue> values = new HashSet<>();
	private final Set<Type> types = new TreeSet<>( new TypeCMP<>() );
	private final Set<Type> producedTypes = new TreeSet<>( new TypeCMP<>() );

	void beforeBeanDiscovery( @Observes BeforeBeanDiscovery event, BeanManager bm )
	{
		addType( CDIConfigRegistry.class, bm, event );
		addType( ConfigFactory.class, bm, event );
		addType( ConfigNodeFactory.class, bm, event );
		addType( INIConfigReader.class, bm, event );
		addType( JMXConfigReader.class, bm, event );
		addType( PRPConfigReader.class, bm, event );
		addType( XMLConfigReader.class, bm, event );
		addType( YMLConfigReader.class, bm, event );
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

	<X> void collectConfigValue( @Observes @WithAnnotations( ConfigValue.class ) ProcessAnnotatedType<X> event )
	{
		final ConfigType<X> type = new ConfigType<>( event.getAnnotatedType() );

		if( type.modified() ) {
			L.info( "Found: %s", type.getBaseType().getTypeName() );

			if( L.isDebugEnabled() ) {
				logMembers( "Constructor", type.getConstructors() );
				logMembers( "Field", type.getFields() );
				logMembers( "Method", type.getMethods() );
			}

			event.setAnnotatedType( type );
		}

		this.values.addAll( type.values() );
	}

	<T, X> void processInjectionPoint( @Observes ProcessInjectionPoint<T, X> event )
	{
		final InjectionPoint p = event.getInjectionPoint();

		if( p.getAnnotated().isAnnotationPresent( ConfigValue.class ) ) {
			Type t = p.getType();

			if( t instanceof ParameterizedType ) {
				if( Instance.class == ( (ParameterizedType) t ).getRawType() ) {
					t = ( (ParameterizedType) t ).getActualTypeArguments()[0];
				}
			}
			L.info( "Config type: %s from %s", t, p );

			this.types.add( wrap( t ) );
		}
	}

	<T, X> void processProducer( @Observes ProcessProducer<T, X> event )
	{
		final AnnotatedMember<T> m = event.getAnnotatedMember();

		if( m.isAnnotationPresent( ConfigValue.class ) && ( m.getBaseType() != Object.class ) ) {
			final Type t = wrap( m.getBaseType() );

			L.info( "Remove type: %s from %s", t, m );

			this.producedTypes.add( t );
		}
	}

	void afterBeanDiscovery( @Observes AfterBeanDiscovery event, BeanManager bm )
	{
		this.types.removeAll( this.producedTypes );

		if( this.types.size() > 0 ) {
			createFactory( event, bm );
		}
	}

//	void afterDeploymentValidation( @Observes AfterDeploymentValidation event, BeanManager bm )
//	{
//		final Set<Bean<?>> beans = bm.getBeans( CDIConfigRegistry.class );
//		final Bean<CDIConfigRegistry> bean = (Bean<CDIConfigRegistry>) bm.resolve( beans );
//		final CreationalContext<CDIConfigRegistry> cx = bm.createCreationalContext( bean );
//		final CDIConfigRegistry reference = (CDIConfigRegistry) bm.getReference( bean, CDIConfigRegistry.class, cx );
//
//		ConfigRegistry.setInstance( reference );
//	}

	private void createFactory( AfterBeanDiscovery event, BeanManager bm )
	{
		L.info( "Add producer for types %s", this.types );

		final AnnotatedTypeW<ConfigFactory> declType = new AnnotatedTypeW<>( bm.createAnnotatedType( ConfigFactory.class ) );
		final AnnotatedMethod<? super ConfigFactory> prodMethod;

		try {
			prodMethod = declType.getMethod( "create", InjectionPoint.class );
		}
		catch( final NoSuchMethodException e ) {
			event.addDefinitionError( e );

			return;
		}

		final Bean<ConfigFactory> declBean = BeanBuilder.<ConfigFactory> create( bm )
			.readType( declType )
			.build();

		event.addBean( declBean );

		final ProducerFactory<? super ConfigFactory> prodFactory = bm.getProducerFactory( prodMethod, declBean );
		final BeanAttributes<?> prodAttribute = bm.createBeanAttributes( prodMethod );
		final BeanAttributes<?> beanAttributes = BeanAttributesBuilder
			.create()
			.readAttributes( prodAttribute )
			.types( this.types )
			.build();

		Bean<?> bean = bm.createBean( beanAttributes, (Class) ConfigFactory.class, prodFactory );

		if( bean instanceof PassivationCapable ) {
			bean = BeanBuilder.create( bm ).readBean( (Bean) bean ).passivationId( null ).build();
		}

		event.addBean( bean );
	}

	Collection<ConfigSource> sources()
	{
		return unmodifiableSet( this.sources );
	}

	Set<ConfigValue> values()
	{
		return unmodifiableSet( this.values );
	}

	private Type wrap( Type t )
	{
		if( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
			t = Primitives.wrap( (Class<?>) t );
		}

		return t;
	}

	private void addType( Class<?> type, BeanManager bm, BeforeBeanDiscovery event )
	{
		AnnotatedType<?> at = bm.createAnnotatedType( type );

		at = new AnnotatedTypeW<>( at );

		if( at.getAnnotations().stream().allMatch( a -> !bm.isScope( a.annotationType() ) ) ) {
			at.getAnnotations().add( ApplicationScoped.Literal.INSTANCE );
		}

		event.addAnnotatedType( at, type.getName() );
	}

	private void addSources( AnnotatedType<?> t, ConfigSource... sources )
	{
		final List<ConfigSource> all = asList( sources );

		L.info( "Source: %s", t.getBaseType() );
		L.debug( "\t%s", all );

		this.sources.addAll( all );
	}

	private <A extends AnnotatedMember<? super X>, X> void logMembers( String prefix, Collection<A> members )
	{
		members.stream().filter( e -> e.isAnnotationPresent( Inject.class ) ).forEach( e -> L.debug( "\t%s: %s/%s", prefix, e, e.getBaseType() ) );
	}
}
