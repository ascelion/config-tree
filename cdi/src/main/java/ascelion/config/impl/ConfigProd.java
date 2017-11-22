
package ascelion.config.impl;

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

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigValue;
import ascelion.config.cvt.Converters;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Typed( ConfigProd.class )
class ConfigProd
{

	static private final Logger L = LoggerFactory.getLogger( ConfigProd.class );

	@Inject
	private BeanManager bm;
	@Inject
	private ConfigExtension ext;
	@Inject
	private Converters conv;
	@Inject
	@Any
	private Instance<ConfigReader> rdi;

	private final ConfigLoad load = new ConfigLoad();
	private ConfigNode root;
	private final Map<Class<? extends ConfigConverter>, InstanceInfo<? extends ConfigConverter>> converters = new IdentityHashMap<>();

	@Produces
	@Dependent
	@ConfigValue( "" )
	Object create( InjectionPoint ip )
	{
		L.trace( "Value: {}", ip.getAnnotated() );

		final ConfigValue a = ip.getAnnotated().getAnnotation( ConfigValue.class );
		final Type t = ip.getType();

		L.trace( format( "%s -> %s ", a.value(), ip.getAnnotated() ) );

		try {
			return this.conv.getValue( t, this.root.getNode( a.value() ), a.unwrap() );
		}
		catch( final ConfigNotFoundException e ) {
			return this.conv.getValue( t, (String) this.root.getValue( a.value() ), a.unwrap() );
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

		this.root = this.load.load( this.ext.sources() );
	}

	@PreDestroy
	private void preDestroy()
	{
		this.converters.values().forEach( InstanceInfo::destroy );
		this.converters.clear();
	}

}
