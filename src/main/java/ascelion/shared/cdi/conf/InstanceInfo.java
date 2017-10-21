
package ascelion.shared.cdi.conf;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import static java.lang.String.format;
import static java.util.Collections.emptySet;

final class InstanceInfo<T>
{

	final Bean<T> bean;
	final CreationalContext<T> context;
	final T instance;
	final Set<? extends Annotation> qualifiers;

	InstanceInfo( BeanManager bm, Bean<T> bean )
	{
		this.bean = bean;
		this.context = bm.createCreationalContext( this.bean );
		this.instance = (T) bm.getReference( bean, bean.getBeanClass(), this.context );
		this.qualifiers = bean.getQualifiers();
	}

	InstanceInfo( T instance )
	{
		this.bean = null;
		this.context = null;
		this.instance = instance;
		this.qualifiers = emptySet();
	}

	<A extends Annotation> A qualifier( Class<A> cls )
	{
		return this.bean.getQualifiers().stream()
			.filter( a -> a.annotationType() == cls )
			.findFirst()
			.map( cls::cast )
			.orElseThrow( () -> new IllegalArgumentException( format( "Cannot find qualifier %s", cls.getName() ) ) );
	}

	void destroy()
	{
		if( this.bean != null && this.context != null ) {
			this.bean.destroy( this.instance, this.context );
		}
	}
}
