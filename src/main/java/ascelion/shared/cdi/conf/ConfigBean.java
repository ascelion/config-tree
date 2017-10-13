
package ascelion.shared.cdi.conf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

@Typed
final class ConfigBean<T> implements Bean<T>, PassivationCapable
{

	final Bean<T> delegate;
	final Set<Type> types;

	ConfigBean( Bean<T> producer, Set<Type> types )
	{
		this.delegate = producer;
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
		return this.delegate.getQualifiers();
	}

	@Override
	public Class<? extends Annotation> getScope()
	{
		return this.delegate.getScope();
	}

	@Override
	public String getName()
	{
		return this.delegate.getName();
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes()
	{
		return this.delegate.getStereotypes();
	}

	@Override
	public T create( CreationalContext<T> creationalContext )
	{
		return this.delegate.create( creationalContext );
	}

	@Override
	public Class<?> getBeanClass()
	{
		return this.delegate.getBeanClass();
	}

	@Override
	public boolean isAlternative()
	{
		return this.delegate.isAlternative();
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints()
	{
		return this.delegate.getInjectionPoints();
	}

	@Override
	public boolean isNullable()
	{
		return this.delegate.isNullable();
	}

	@Override
	public void destroy( T instance, CreationalContext<T> creationalContext )
	{
		this.delegate.destroy( instance, creationalContext );
	}

	@Override
	public String getId()
	{
		return getClass().getGenericSuperclass().getTypeName();
	}
}
