
package ascelion.config.cdi;

import static ascelion.cdi.metadata.AnnotatedTypeModifier.makeQualifier;
import static java.util.stream.Collectors.joining;

import ascelion.cdi.bean.BeanAttributesModifier;
import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.config.api.ConfigValue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProducerFactory;
import javax.enterprise.inject.spi.WithAnnotations;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings( "unchecked" )
@Slf4j
public class ConfigExtension implements Extension
{

	private AnnotatedType<ConfigValueProducer> prodType;
	private Bean<ConfigValueProducer> prodBean;

	private final Map<Class<?>, ConfigProcessor<?>> processors = new IdentityHashMap<>();
	private final Set<Type> types = new HashSet<>();
	private final Set<Type> skippedTypes = new HashSet<>();

	void beforeBeanDiscovery( BeanManager bm, @Observes BeforeBeanDiscovery event )
	{
		event.addQualifier( makeQualifier( bm.createAnnotatedType( ConfigValue.class ) ) );

		log.info( "Created qualifier @ConfigValue" );

		this.prodType = bm.createAnnotatedType( ConfigValueProducer.class );

		event.addAnnotatedType( this.prodType, ConfigValueProducer.class.getName() );
		event.addAnnotatedType( BeanConverterFactory.class, BeanConverterFactory.class.getName() );
		event.addAnnotatedType( CDIConfigProvider.class, CDIConfigProvider.class.getName() );
	}

	void processConfigProducerBean( @Observes ProcessManagedBean<ConfigValueProducer> event )
	{
		this.prodBean = event.getBean();
	}

	<X> void processConfigValue( BeanManager bm,
		@Observes @WithAnnotations( ConfigValue.class ) ProcessAnnotatedType<X> event )
	{

		AnnotatedType<X> type = event.getAnnotatedType();

		log.info( "Processing type {}", type );

		final ConfigProcessor<X> processor = new ConfigProcessor<>( type );

		if( processor.values().size() > 0 ) {
			type = processor.type();

			log.info( "Updated type {}", type );

			event.setAnnotatedType( type );

			this.processors.put( type.getJavaClass(), processor );
		}
	}

	<T, X> void processInjectionPoint( BeanManager bm, @Observes ProcessInjectionPoint<T, X> event )
	{
		final InjectionPoint ijp = event.getInjectionPoint();
		final Annotated annotated = ijp.getAnnotated();

		if( annotated.isAnnotationPresent( ConfigValue.class ) ) {
			final Type type = ijp.getType();

			if( this.types.add( type ) ) {
				log.debug( "May need to create @ConfigValue producer for {}", type );
			}
		}
	}

	<X> void processInjectionTarget( BeanManager bm, @Observes ProcessInjectionTarget<X> event )
	{
		final Class<X> javaClass = event.getAnnotatedType().getJavaClass();
		final ConfigProcessor<X> processor = (ConfigProcessor<X>) this.processors.get( javaClass );

		if( processor != null ) {
			final InjectionTarget<X> it = event.getInjectionTarget();

			log.info( "Overring injection of {}", it );

			event.setInjectionTarget( new ConfigInjectionTarget<>( bm, it, processor ) );
		}
	}

	void processProducer( BeanManager bm, @Observes ProcessProducer<?, ?> event )
	{
		final AnnotatedMember<?> annotated = event.getAnnotatedMember();

		if( annotated.isAnnotationPresent( ConfigValue.class ) ) {
			final Type type = event.getAnnotatedMember().getBaseType();

			if( this.skippedTypes.add( type ) ) {
				log.debug( "Will not create @ConfigValue producer for {} -- ", type, annotated );

				if( type instanceof ParameterizedType ) {
					this.skippedTypes.add( ( (ParameterizedType) type ).getRawType() );
				}
			}
		}
	}

	void afterBeanDiscovery( BeanManager bm, @Observes AfterBeanDiscovery event )
	{
		this.types.removeIf( this::filterType );

		if( this.types.isEmpty() ) {
			return;
		}

		if( log.isInfoEnabled() ) {
			log.info( "Adding @ConfigValue producer(s) for {}", this.types.stream().map( Type::getTypeName ).collect( joining( ", " ) ) );
		}

		final AnnotatedMethod<? super ConfigValueProducer> method = AnnotatedTypeModifier.create( this.prodType )
			.method( "produceValue", InjectionPoint.class ).get();
		final ProducerFactory<ConfigValueProducer> prodFactory = bm.getProducerFactory( method, this.prodBean );
		final BeanAttributesModifier<?> prodAttributes = BeanAttributesModifier.create( bm.createBeanAttributes( method ) );

		prodAttributes.types().clear().addAll( this.types );

		final Bean<?> prodBean = bm.createBean( prodAttributes.get(), ConfigValueProducer.class, prodFactory );

		event.addBean( prodBean );
	}

	@SuppressWarnings( { "rawtypes" } )
	private boolean filterType( Type type )
	{
		if( this.skippedTypes.contains( type ) ) {
			return true;
		}

		if( type instanceof ParameterizedType ) {
			final Type rawType = ( (ParameterizedType) type ).getRawType();

			if( this.skippedTypes.contains( rawType ) ) {
				return true;
			}
			if( rawType instanceof Class ) {
				final Class rawClass = (Class) rawType;

				return this.skippedTypes.stream()
					.filter( t -> Class.class.isInstance( t ) )
					.map( Class.class::cast )
					.anyMatch( c -> c.isAssignableFrom( rawClass ) );
			}
		}

		return false;
	}
}
