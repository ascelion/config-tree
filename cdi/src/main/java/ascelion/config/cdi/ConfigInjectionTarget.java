package ascelion.config.cdi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Inject;

import ascelion.config.api.ConfigRoot;
import ascelion.config.api.ConfigValue;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
class ConfigInjectionTarget<T> implements InjectionTarget<T> {
	static private final Logger LOG = LoggerFactory.getLogger(ConfigInjectionTarget.class);

	private final BeanManager bm;
	private final InjectionTarget<T> delegate;
	private final ConfigProcessor<T> processor;

	@Override
	public T produce(CreationalContext<T> ctx) {
		return this.delegate.produce(ctx);
	}

	@Override
	public void inject(T instance, CreationalContext<T> ctx) {
		this.delegate.inject(instance, ctx);

		final Bean<?> cfb = this.bm.getBeans(ConfigRoot.class).iterator().next();
		final CreationalContext<?> ccx = this.bm.createCreationalContext(cfb);
		final ConfigRoot root = (ConfigRoot) this.bm.getReference(cfb, ConfigRoot.class, ccx);

		this.processor.fields().stream()
				.filter(f -> f.getAnnotation(Inject.class) == null)
				.forEach(f -> inject(root, instance, f));
		this.processor.methods().stream()
				.filter(m -> m.getAnnotation(Inject.class) == null)
				.forEach(m -> inject(root, instance, m));
	}

	@Override
	public void postConstruct(T instance) {
		this.delegate.postConstruct(instance);
	}

	@Override
	public void dispose(T instance) {
		this.delegate.dispose(instance);
	}

	@Override
	public void preDestroy(T instance) {
		this.delegate.preDestroy(instance);
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return this.delegate.getInjectionPoints();
	}

	@SneakyThrows
	static <T> void inject(ConfigRoot root, T instance, AnnotatedField<T> annotated) {
		final ConfigValue cval = annotated.getAnnotation(ConfigValue.class);
		final String prop = cval.value();
		final Type type = annotated.getBaseType();
		final Optional<?> value = root.getValue(prop, type);

		if (value.isPresent()) {
			final Field field = annotated.getJavaMember();

			LOG.debug("Invoking setter {}", field);

			field.setAccessible(true);
			field.set(instance, value.get());
		}
	}

	@SneakyThrows
	static <T> void inject(ConfigRoot root, T instance, AnnotatedMethod<T> annotated) {
		final AnnotatedParameter<T> param = annotated.getParameters().get(0);
		final ConfigValue cval = param.getAnnotation(ConfigValue.class);
		final String prop = cval.value();
		final Type type = param.getBaseType();
		final Optional<?> value = root.getValue(prop, type);

		if (value.isPresent()) {
			final Method method = annotated.getJavaMember();

			LOG.debug("Invoking setter {}", annotated);

			method.setAccessible(true);
			method.invoke(instance, value.get());
		}
	}
}
