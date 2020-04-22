
package ascelion.config.microprofile;

import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class InstanceProvider<T>
{

	private final Class<T> type;
	private final Supplier<T> provider;

	T get()
	{
		try {
			return get( CDI.current().getBeanManager() );
		}
		catch( IllegalStateException | NoClassDefFoundError e ) {
			;
		}

		return this.provider.get();
	}

	@SuppressWarnings( "unchecked" )
	private T get( @NonNull BeanManager bm )
	{
		final Set<Bean<?>> beans = bm.getBeans( this.type );

		if( beans.isEmpty() ) {
			return this.provider.get();
		}
		if( beans.size() > 1 ) {
			throw new AmbiguousResolutionException( "Ambigous bean definition for ConfigRoot" );
		}

		final Bean<?> bean = beans.iterator().next();
		final CreationalContext<?> cc = bm.createCreationalContext( bean );

		return (T) bm.getReference( bean, this.type, cc );
	}
}
