
package ascelion.config.cdi.jmx;

import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.management.MBeanServer;

import ascelion.config.api.ConfigReader;
import ascelion.config.utils.LazyValue;

import static java.util.Collections.emptyMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;

@ConfigReader.Type( value = JMXConfigReader.TYPE )
public class JMXConfigReader implements ConfigReader
{

	static public final String TYPE = "JMX";

	private final LazyValue<JMXDynamicMap> values = new LazyValue<>();

	@Inject
	private Instance<Config> cf;
	@Inject
	private Instance<MBeanServer> mbsi;

	private MBeanServer mbs;

	@Override
	public boolean isModified( String domain )
	{
		return this.mbs == null || getMap( domain ).isChanged();
	}

	@Override
	public Map<String, String> readConfiguration( String domain )
	{
		if( enabled() ) {
			final JMXDynamicMap map = getMap( domain );

			try {
				return map;
			}
			finally {
				map.setChanged( false );
			}
		}

		return enabled() ? getMap( domain ) : emptyMap();
	}

	private boolean enabled()
	{
		try {
			return this.mbs != null
				|| ( this.mbsi != null ) && ( this.mbs = this.mbsi.get() ) != null;
		}
		catch( final UnsatisfiedResolutionException e ) {
			return false;
		}
	}

	private JMXDynamicMap getMap( String domain )
	{
		return this.values.get( () -> new JMXDynamicMap( this.mbs, domain, this::getValue ) );
	}

	private String getValue( String path )
	{
		for( final ConfigSource cs : this.cf.get().getConfigSources() ) {
			final String val = cs.getValue( path );

			if( val != null ) {
				return val;
			}
		}

		return null;
	}
}
