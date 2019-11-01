package ascelion.config.cdi;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ConfigInjectionTarget<T> implements InjectionTarget<T> {
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

//		final ConfigProducer beans = new ConfigProducer(this.bm, ConfigInstance.get(this.bm));
//
//		this.inject.fields().stream()
//				.filter(f -> f.getAnnotation(Inject.class) == null)
//				.forEach(f -> beans.inject(instance, f));
//		this.inject.methods().stream()
//				.filter(m -> m.getAnnotation(Inject.class) == null)
//				.forEach(m -> beans.inject(instance, m));
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

}
