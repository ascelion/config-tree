
package ascelion.config.impl;

import java.util.function.Supplier;

import ascelion.config.api.ConfigNode;
import ascelion.config.impl.ConfigNodeImpl.ConfigNodeTA;
import ascelion.logging.LOG;

import com.google.gson.GsonBuilder;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public final class ConfigNodeFactory implements Supplier<ConfigNode>
{

	static private final LOG L = LOG.get();

	@Override
	public ConfigNode get()
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
