
package ascelion.cdi.conf;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import ascelion.cdi.conf.ConfigValue;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Typed( ConfigProd.class )
class ConfigProd extends ConfigProdBase
{

	static private final Logger L = LoggerFactory.getLogger( ConfigProd.class );

	@Inject
	private BeanManager bm;

	@Inject
	private ConfigExtension ext;

	private final Map<Class<? extends BiFunction>, InstanceInfo<? extends BiFunction>> converters = new IdentityHashMap<>();

	@Produces
	@Dependent
	@ConfigValue( "" )
	Object create( InjectionPoint ip )
	{
		L.trace( "Value: {}", ip.getAnnotated() );

		final ConfigValue a = annotation( ip );
		final BiFunction<Class<?>, String, ?> f = this.converters.computeIfAbsent( a.converter(), x -> {
			// shouldn't happen
			throw new IllegalStateException( format( "Cannot find converter of type %s", x.getName() ) );
		} ).instance;

		final Type t = ip.getType();

		return new Converter( this.cc.root(), a, f, t ).convert();
	}

	@PostConstruct
	private void postConstruct()
	{
		this.ext.converters().forEach( c -> {
			final Set<Bean<?>> beans = this.bm.getBeans( c );
			final InstanceInfo<BiFunction> info;

			if( beans.size() > 0 ) {
				info = new InstanceInfo<>( this.bm, (Bean<BiFunction>) this.bm.resolve( beans ) );
			}
			else {
				try {
					info = new InstanceInfo<>( c.newInstance() );
				}
				catch( InstantiationException | IllegalAccessException e ) {
					throw new IllegalStateException( e );
				}
			}

			this.converters.put( c, info );
		} );
	}

	@PreDestroy
	private void preDestroy()
	{
		this.converters.values().forEach( InstanceInfo::destroy );
		this.converters.clear();
	}

}
