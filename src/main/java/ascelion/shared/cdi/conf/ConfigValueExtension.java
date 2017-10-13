
package ascelion.shared.cdi.conf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Inject;

import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigValueExtension implements Extension
{

	@ApplicationScoped
	@Typed( ConfigProducer.class )
	static class ConfigProducer
	{

		@Produces
		@ConfigValue( value = "", unwrap = "" )
		Object create( InjectionPoint ip )
		{
			return null;
		}
	}

	@Typed
	static final class ConfigBean<T> implements Bean<T>
	{

		final Bean<T> producer;
		final Set<Type> types;

		ConfigBean( Bean<T> producer, Set<Type> types )
		{
			this.producer = producer;
			this.types = types;
		}

		@Override
		public Set<Type> getTypes()
		{
			return this.types;
		}

		@Override
		public Set<Annotation> getQualifiers()
		{
			return this.producer.getQualifiers();
		}

		@Override
		public Class<? extends Annotation> getScope()
		{
			return this.producer.getScope();
		}

		@Override
		public String getName()
		{
			return this.producer.getName();
		}

		@Override
		public Set<Class<? extends Annotation>> getStereotypes()
		{
			return this.producer.getStereotypes();
		}

		@Override
		public T create( CreationalContext<T> creationalContext )
		{
			return this.producer.create( creationalContext );
		}

		@Override
		public Class<?> getBeanClass()
		{
			return this.producer.getBeanClass();
		}

		@Override
		public boolean isAlternative()
		{
			return this.producer.isAlternative();
		}

		@Override
		public Set<InjectionPoint> getInjectionPoints()
		{
			return this.producer.getInjectionPoints();
		}

		@Override
		public boolean isNullable()
		{
			return this.producer.isNullable();
		}

		@Override
		public void destroy( T instance, CreationalContext<T> creationalContext )
		{
			this.producer.destroy( instance, creationalContext );
		}
	}

	static private final Logger L = LoggerFactory.getLogger( ConfigValueExtension.class );

	private final Set<Type> types = new HashSet<>();

	private Bean<ConfigProducer> producer;

	<X> void processAnnotatedType( BeanManager bm, @Observes @WithAnnotations( ConfigValue.class ) ProcessAnnotatedType<X> event )
	{
		final AnnotatedType<X> type = event.getAnnotatedType();

		L.debug( "Found: {}", type );

		final List<AnnotatedField<? super X>> configs = type.getFields().stream()
			.filter( f -> f.isAnnotationPresent( ConfigValue.class ) )
			.filter( f -> {
				this.types.add( f.getBaseType() );

				return !f.isAnnotationPresent( Inject.class );
			} )
			.collect( Collectors.toList() );

		configs.forEach( f -> L.debug( "Field: {}", f ) );

		final AnnotatedTypeBuilder<X> atb = new AnnotatedTypeBuilder<>();

		atb.readFromType( type );

		configs.forEach( f -> {
			atb.addToField( f, AnnotationInstanceProvider.of( Inject.class ) );
		} );

		event.setAnnotatedType( atb.create() );
	}

	void afterTypeDiscovery( BeanManager bm, @Observes AfterTypeDiscovery event )
	{
		final AnnotatedType<ConfigProducer> at = bm.createAnnotatedType( ConfigProducer.class );

		event.addAnnotatedType( at, at.getJavaClass().getName() );
	}

	public void findDynamicProducer( @Observes ProcessBean<ConfigProducer> event )
	{
		this.producer = event.getBean();
	}

	void afterBeanDiscovery( BeanManager bm, @Observes AfterBeanDiscovery event )
	{
		event.addBean( new ConfigBean<>( this.producer, this.types ) );
	}

}
