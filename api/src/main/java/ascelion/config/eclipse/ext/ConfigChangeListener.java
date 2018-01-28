
package ascelion.config.eclipse.ext;

import java.util.EventListener;

public interface ConfigChangeListener extends EventListener
{

	void sourceChanged( ConfigSourceExt cs );
}
