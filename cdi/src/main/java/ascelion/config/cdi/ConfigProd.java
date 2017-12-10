
package ascelion.config.cdi;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.management.MBeanServer;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigValue;
import ascelion.config.conv.Converters;
import ascelion.config.impl.ConfigLoad;
import ascelion.config.impl.JMXSupport;
import ascelion.config.read.JMXConfigReader;
import ascelion.logging.LOG;

import static java.util.Optional.ofNullable;

@ApplicationScoped
@Typed( ConfigProd.class )
class ConfigProd
{

	static private final LOG L = LOG.get();

	@Inject
	private BeanManager bm;
	@Inject
	private ConfigExtension ext;
	@Inject
	private Converters conv;
	@Inject
	@Any
	private Instance<ConfigReader> rdi;
	@Inject
	@Any
	private Instance<ConfigConverter> cvi;
	@Inject
	@Any
	private Instance<MBeanServer> mbsi;

	private final ConfigLoad load = new ConfigLoad();
	private ConfigNode root;
	private final Map<Class<?>, InstanceInfo<ConfigConverter>> converters = new IdentityHashMap<>();

	@Produces
	@Dependent
	@ConfigValue( "" )
	Object create( InjectionPoint ip )
	{
		L.trace( "Value: %s", ip.getAnnotated() );

		final ConfigValue a = ip.getAnnotated().getAnnotation( ConfigValue.class );
		final Type t = ip.getType();

		L.trace( "%s -> %s ", a.value(), ip.getAnnotated() );

		final ConfigConverter<?> c = ofNullable( this.converters.get( a.converter() ) ).map( i -> i.instance ).orElse( this.conv );

		try {
			return c.create( t, this.root.getNode( a.value() ), a.unwrap() );
		}
		catch( final ConfigNotFoundException e ) {
			return c.create( t, this.root.getValue( a.value() ) );
		}
	}

	@PostConstruct
	private void postConstruct()
	{
		this.conv.setRootNode( () -> this.root );

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

			this.conv.register( info.instance );
			this.converters.put( c, info );
		} );

		this.rdi.forEach( this.load::addReader );
		this.load.addSources( this.ext.sources() );

		this.root = this.load.load();

		this.ext.sources().stream()
			.filter( s -> s.type().equals( JMXConfigReader.TYPE ) )
			.forEach( s -> {
				final JMXSupport sup = new JMXSupport( this.mbsi.get(), s.value() );

				sup.buildEntries( this.root );
			} );
	}

	@PreDestroy
	private void preDestroy()
	{
		this.converters.values().forEach( InstanceInfo::destroy );
		this.converters.clear();
	}

}
