
package ascelion.config.impl;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.ConfigNodeImpl.ConfigNodeTA;
import ascelion.logging.LOG;

import static java.util.Arrays.asList;

import com.google.gson.GsonBuilder;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public final class ConfigLoad
{

	static private final LOG L = LOG.get();

	public void addReader( ConfigReader rd )
	{
		ConfigRegistry.getInstance().addReaders( asList( rd ) );
	}

	public void addReaders( ConfigReader... rds )
	{
		ConfigRegistry.getInstance().addReaders( asList( rds ) );
	}

	public void addSource( ConfigSource cs )
	{
		ConfigRegistry.getInstance().addSources( asList( cs ) );
	}

	public void addSources( ConfigSource... css )
	{
		ConfigRegistry.getInstance().addSources( asList( css ) );
	}

	public ConfigNode load()
	{
		final ConfigNodeImpl root = new ConfigNodeImpl();
		final Config config = ConfigProviderResolver.instance().getConfig();

		try {
			config.getConfigSources().forEach( cs -> {
				root.setValue( cs.getProperties() );
			} );

			if( L.isTraceEnabled() ) {
				final String s = new GsonBuilder()
					.setPrettyPrinting()
					.registerTypeHierarchyAdapter( ConfigNode.class, new ConfigNodeTA() )
					.create()
					.toJson( root );

				L.trace( "Config: %s", s );
			}
		}
		finally {
			ConfigProviderResolver.instance().releaseConfig( config );
		}

		return root;
	}
}
