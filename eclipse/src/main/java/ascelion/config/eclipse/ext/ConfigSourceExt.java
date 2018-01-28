
package ascelion.config.eclipse.ext;

import org.eclipse.microprofile.config.spi.ConfigSource;

public interface ConfigSourceExt extends ConfigSource
{

	default void addChangeListener( ConfigChangeListener cl )
	{
	}

	default void removeChangeListener( ConfigChangeListener cl )
	{
	}
}
