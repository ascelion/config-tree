
package ascelion.config.eclipse.ext;

import java.util.Collection;
import java.util.LinkedHashSet;

import static java.util.Collections.synchronizedSet;

public final class ConfigChangeListenerSupport
{

	public ConfigChangeListenerSupport( ConfigSourceExt csx )
	{
		this.csx = csx;
	}

	private final ConfigSourceExt csx;
	private final Collection<ConfigChangeListener> list = synchronizedSet( new LinkedHashSet<>() );

	public void fireChanged()
	{
		for( final ConfigChangeListener cl : this.list.toArray( new ConfigChangeListener[0] ) ) {
			cl.sourceChanged( this.csx );
		}
	}

	public void add( ConfigChangeListener cl )
	{
		this.list.add( cl );
	}

	public void remove( ConfigChangeListener cl )
	{
		this.list.remove( cl );
	}
}
