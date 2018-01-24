
package ascelion.config.eclipse.cdi;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProducerFactory;

import ascelion.cdi.bean.BeanAttributesBuilder;
import ascelion.cdi.bean.BeanBuilder;
import ascelion.cdi.type.AnnotatedTypeW;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class ConfigExtension implements Extension
{

	private final Collection<Type> types = new HashSet<>();

	void beforeBeanDiscovery( @Observes BeforeBeanDiscovery event, BeanManager bm )
	{
		addTypes( bm, event, ConfigFactory.class );
	}

	private void addTypes( BeanManager bm, BeforeBeanDiscovery event, Class<?>... types )
	{
		for( final Class<?> type : types ) {
			event.addAnnotatedType( bm.createAnnotatedType( type ) );
		}
	}

	<T, X> void processInjectionPoint( @Observes ProcessInjectionPoint<T, X> event )
	{
		if( event.getInjectionPoint().getAnnotated().isAnnotationPresent( ConfigProperty.class ) ) {
			this.types.add( event.getInjectionPoint().getType() );
		}
	}

	void afterBeanDiscovery( BeanManager bm, @Observes AfterBeanDiscovery event )
	{
		final AnnotatedTypeW<ConfigPropertyFactory> declType = new AnnotatedTypeW<>( bm.createAnnotatedType( ConfigPropertyFactory.class ) );
		final AnnotatedMethod<? super ConfigPropertyFactory> prodMethod;

		try {
			prodMethod = declType.getMethod( "getValue", InjectionPoint.class );
		}
		catch( final NoSuchMethodException e ) {
			event.addDefinitionError( e );

			return;
		}

		final Bean<ConfigPropertyFactory> declBean = BeanBuilder.<ConfigPropertyFactory> create( bm )
			.beanClass( ConfigPropertyFactory.class )
			.types( ConfigPropertyFactory.class )
			.scope( ApplicationScoped.class )
			.build();

		final ProducerFactory<? super ConfigPropertyFactory> prodFactory = bm.getProducerFactory( prodMethod, declBean );
		final BeanAttributes<?> prodAttribute = bm.createBeanAttributes( prodMethod );
		final BeanAttributes<?> beanAttributes = BeanAttributesBuilder
			.create()
			.readAttributes( prodAttribute )
			.types( this.types )
			.build();

		Bean<?> bean = bm.createBean( beanAttributes, (Class) ConfigPropertyFactory.class, prodFactory );

		if( bean instanceof PassivationCapable ) {
			bean = BeanBuilder.create( bm ).readBean( (Bean) bean ).passivationId( null ).build();
		}

		event.addBean( bean );
	}
}
