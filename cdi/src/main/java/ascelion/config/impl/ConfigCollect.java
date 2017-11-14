
package ascelion.config.impl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
class ConfigCollect
{

	static private final Logger L = LoggerFactory.getLogger( ConfigCollect.class );

	@Inject
	private ConfigExtension ext;
	@Inject
	@Any
	private Instance<ConfigReader> rdi;

	private final ConfigLoad load = new ConfigLoad();
	private ConfigNode root;

	@Produces
	@Dependent
	@Typed( ConfigNode.class )
	synchronized ConfigNodeImpl root()
	{
		return (ConfigNodeImpl) this.root;
	}

	synchronized void readConfiguration( @Observes ConfigSource source )
	{
		this.load.load( source, this.root );
	}

	@PostConstruct
	private void postConstruct()
	{
		this.rdi.forEach( this.load::addReader );

		this.root = this.load.load( this.ext.sources() );

		// XXX
		this.root.asMap( x -> x )
			.forEach( ( k, v ) -> {
				this.root.setValue( k, System.getProperty( k, v ) );
			} );
	}
}
