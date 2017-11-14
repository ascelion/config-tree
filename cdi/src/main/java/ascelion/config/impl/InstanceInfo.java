
package ascelion.config.impl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

final class InstanceInfo<T>
{

	final Bean<T> bean;
	final CreationalContext<T> context;
	final T instance;

	InstanceInfo( BeanManager bm, Bean<T> bean )
	{
		this.bean = bean;
		this.context = bm.createCreationalContext( this.bean );
		this.instance = (T) bm.getReference( bean, bean.getBeanClass(), this.context );
	}

	InstanceInfo( T instance )
	{
		this.bean = null;
		this.context = null;
		this.instance = instance;
	}

	void destroy()
	{
		if( this.bean != null && this.context != null ) {
			this.bean.destroy( this.instance, this.context );
		}
	}
}
