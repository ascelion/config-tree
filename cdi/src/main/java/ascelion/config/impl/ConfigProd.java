
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

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

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigValue;

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

	@Inject
	private Converters cvt;

	private InstanceInfo<? extends ConfigConverter> cvti;

	private final Map<Class<? extends ConfigConverter>, InstanceInfo<? extends ConfigConverter>> converters = new IdentityHashMap<>();

	@Produces
	@Dependent
	@ConfigValue( "" )
	Object create( InjectionPoint ip )
	{
		L.trace( "Value: {}", ip.getAnnotated() );

		final ConfigValue a = annotation( ip );
		final Type t = ip.getType();
		final Object v = new TypedValue( this.cc.root(), a, t, x -> getConverter( x ) ).get();

		return v;
	}

	private ConfigConverter getConverter( Class<? extends ConfigConverter> type )
	{
		return this.converters.computeIfAbsent( type, x -> this.cvti ).instance;
	}

	@PostConstruct
	private void postConstruct()
	{
		this.ext.converters().forEach( c -> {
			final InstanceInfo<ConfigConverter> info;
			final Set<Bean<?>> beans = this.bm.getBeans( c );

			if( beans.size() > 0 ) {
				info = new InstanceInfo<>( this.bm, (Bean<ConfigConverter>) this.bm.resolve( beans ) );
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

		this.cvti = new InstanceInfo<>( this.cvt );
	}

	@PreDestroy
	private void preDestroy()
	{
		this.converters.values().forEach( InstanceInfo::destroy );
		this.converters.clear();
	}

}
