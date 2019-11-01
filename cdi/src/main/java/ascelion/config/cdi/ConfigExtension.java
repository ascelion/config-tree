package ascelion.config.cdi;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedMember;
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
import javax.enterprise.inject.spi.WithAnnotations;

import ascelion.config.api.ConfigValue;

import static ascelion.cdi.metadata.AnnotatedTypeModifier.makeQualifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigExtension implements Extension {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigExtension.class);

	private AnnotatedType<ConfigValueProducer> prodType;
	private Bean<ConfigValueProducer> prodBean;

	private final Map<Class<?>, ConfigProcessor<?>> processors = new IdentityHashMap<>();
	private final Set<Type> types = new HashSet<>();
	private final Set<Type> skippedTypes = new HashSet<>();

	void beforeBeanDiscovery(BeanManager bm, @Observes BeforeBeanDiscovery event) {
		event.addQualifier(makeQualifier(bm.createAnnotatedType(ConfigValue.class)));

		LOG.info("Created qualifier @ConfigValue");

		this.prodType = bm.createAnnotatedType(ConfigValueProducer.class);

		event.addAnnotatedType(this.prodType, ConfigValueProducer.class.getName());
		event.addAnnotatedType(ConfigProvider.class, ConfigProvider.class.getName());
	}

	void processConfigProducerBean(@Observes ProcessManagedBean<ConfigValueProducer> event) {
		this.prodBean = event.getBean();
	}

	@SuppressWarnings("unchecked")
	<X> void processConfigValue(BeanManager bm,
			@Observes @WithAnnotations(ConfigValue.class) ProcessAnnotatedType<X> event) {

		AnnotatedType<X> type = event.getAnnotatedType();

		LOG.info("Processing type {}", type);

		final ConfigProcessor<X> processor = new ConfigProcessor<>(type);

		if (processor.values().size() > 0) {
			type = processor.type();

			LOG.info("Updated type {}", type);

			event.setAnnotatedType(type);

			this.processors.put(type.getJavaClass(), processor);
		}
	}

	<T, X> void processInjectionPoint(BeanManager bm, @Observes ProcessInjectionPoint<T, X> event) {
		final InjectionPoint ijp = event.getInjectionPoint();
		final Annotated annotated = ijp.getAnnotated();

		if (annotated.isAnnotationPresent(ConfigValue.class)) {
			final Type type = ijp.getType();

			if (this.types.add(type)) {
				LOG.debug("May need to create @ConfigValue producer for {}", type);
			}
		}
	}

	<X> void processInjectionTarget(BeanManager bm, @Observes ProcessInjectionTarget<X> event) {
		final Class<X> javaClass = event.getAnnotatedType().getJavaClass();
		final ConfigProcessor<X> processor = (ConfigProcessor<X>) this.processors.get(javaClass);

		if (processor != null) {
			final InjectionTarget<X> it = event.getInjectionTarget();

			LOG.info("Overring injection of {}", it);

			event.setInjectionTarget(new ConfigInjectionTarget<>(bm, it, processor));
		}
	}

	void processProducer(BeanManager bm, @Observes ProcessProducer<?, ?> event) {
		final AnnotatedMember<?> annotated = event.getAnnotatedMember();

		if (annotated.isAnnotationPresent(ConfigValue.class)) {
			final Type type = event.getAnnotatedMember().getBaseType();

			if (this.skippedTypes.add(type)) {
				LOG.debug("Will not create @ConfigValue producer for {}", type);

				if (type instanceof ParameterizedType) {
					this.skippedTypes.add(((ParameterizedType) type).getRawType());
				}
			}
		}
	}

}
