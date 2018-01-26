
package ascelion.config.cdi;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.CDI;

import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.ConfigSources;

final class CDIConfigSources extends ConfigSources
{

	@Override
	protected Iterable<ConfigReader> loadReaders( ClassLoader cld )
	{
		return CDI.current().select( ConfigReader.class, Any.Literal.INSTANCE );
	}

	@Override
	protected Iterable<ConfigSource> loadSources( ClassLoader cld )
	{
		return CDI.current().getBeanManager().getExtension( ConfigExtension.class ).sources();
	}
}
