
package ascelion.config.cdi;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.management.MBeanServer;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigValue;
//import ascelion.config.impl.JMXSupport;
//import ascelion.config.read.JMXConfigReader;
import ascelion.logging.LOG;

class ConfigFactory
{

	static private final LOG L = LOG.get();

	@Inject
	private BeanManager bm;
	@Inject
	@Any
	private Instance<ConfigConverter<?>> cvi;
	@Inject
	@Any
	private Instance<MBeanServer> mbsi;
	@Inject
	private ConfigNode root;

	private final Map<ConfigValue, ConfigConverter<?>> converters = new IdentityHashMap<>();

	@Produces
	@ConfigValue( "" )
	Object create( InjectionPoint ip )
	{
		L.trace( "Value: %s", ip.getAnnotated() );

		final ConfigValue a = ip.getAnnotated().getAnnotation( ConfigValue.class );
		final Type t = ip.getType();

		L.trace( "%s -> %s ", a.value(), ip.getAnnotated() );

		final ConfigConverter<?> c = getConverter( t, a );

		try {
			return c.create( this.root.getNode( a.value() ), a.unwrap() );
		}
		catch( final ConfigNotFoundException e1 ) {
			try {
				return c.create( this.root.getValue( a.value() ) );
			}
			catch( final ConfigNotFoundException e2 ) {
				return c.create( null, a.unwrap() );
			}
		}
	}

	private ConfigConverter<?> getConverter( Type t, ConfigValue a )
	{
		if( a.converter() != ConfigConverter.class ) {
			return this.converters.computeIfAbsent( a, this::create );
		}

		return ConfigRegistry.getInstance()
			.converters( getClass().getClassLoader() )
			.getConverter( t );
	}

	private ConfigConverter<?> create( ConfigValue a )
	{
		final Class<? extends ConfigConverter<?>> c = (Class<? extends ConfigConverter<?>>) a.converter();
		final Set<Bean<?>> beans = this.bm.getBeans( c );

		if( beans.size() > 0 ) {
			final Bean<? extends ConfigConverter<?>> bean = (Bean<? extends ConfigConverter<?>>) this.bm.resolve( beans );
			final CreationalContext<? extends ConfigConverter<?>> cx = this.bm.createCreationalContext( bean );
			return (ConfigConverter<?>) this.bm.getReference( bean, bean.getBeanClass(), cx );
		}
		else {
			try {
				return c.newInstance();
			}
			catch( InstantiationException | IllegalAccessException e ) {
				throw new IllegalStateException( e );
			}
		}
	}

}
