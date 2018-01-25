
package ascelion.config.eclipse.cdi;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProducerFactory;

import ascelion.cdi.bean.BeanAttributesBuilder;
import ascelion.cdi.bean.BeanBuilder;
import ascelion.cdi.type.AnnotatedTypeW;
import ascelion.config.eclipse.ConfigInternal;

import static java.util.stream.Collectors.joining;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class ConfigExtension implements Extension
{

	private final Collection<InjectionPoint> validate = new HashSet<>();
	private final Collection<Type> types = new HashSet<>();

	void beforeBeanDiscovery( @Observes BeforeBeanDiscovery event, BeanManager bm )
	{
		addTypes( bm, event, ConfigFactory.class );
	}

	private void addTypes( BeanManager bm, BeforeBeanDiscovery event, Class<?>... types )
	{
		for( final Class<?> type : types ) {
			final AnnotatedType<?> at = bm.createAnnotatedType( type );

			event.addAnnotatedType( at, at.getJavaClass().getCanonicalName() + "#" );
		}
	}

	<T, X> void processInjectionPoint( @Observes ProcessInjectionPoint<T, X> event )
	{
		final InjectionPoint ip = event.getInjectionPoint();

		if( ip.getAnnotated().isAnnotationPresent( ConfigProperty.class ) ) {
			final Type type = ip.getType();

			this.types.add( type );

			if( type instanceof Class ) {
				this.validate.add( ip );
			}
		}
	}

	void afterDeploymentValidation( @Observes AfterDeploymentValidation event )
	{
		if( this.validate.size() > 0 ) {
			final Set<String> missing = new TreeSet<>();
			final ConfigInternal cf = ConfigFactory.getConfig();

			try {
				for( final InjectionPoint ip : this.validate ) {
					final Object pv = ConfigPropertyFactory.getValue( ip, cf );

					if( pv == null ) {
						missing.add( ConfigPropertyFactory.propertyName( ip ) );
					}
				}

				if( missing.size() > 0 ) {
					final String text = "Configuration problem, the following properties are not defined: " +
						missing.stream().collect( joining( ", " ) );

					event.addDeploymentProblem( new DeploymentException( text ) );
				}
			}
			finally {
				ConfigFactory.release( cf );
			}
		}
	}

	void afterBeanDiscovery( BeanManager bm, @Observes AfterBeanDiscovery event )
	{
		final AnnotatedTypeW<ConfigPropertyFactory> declType = new AnnotatedTypeW<>( bm.createAnnotatedType( ConfigPropertyFactory.class ) );
		final AnnotatedMethod<? super ConfigPropertyFactory> prodMethod;

		try {
			prodMethod = declType.getMethod( "getValue", InjectionPoint.class, ConfigInternal.class );
		}
		catch( final NoSuchMethodException e ) {
			event.addDefinitionError( e );

			return;
		}

		final ProducerFactory<? super ConfigPropertyFactory> prodFactory = bm.getProducerFactory( prodMethod, null );
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
