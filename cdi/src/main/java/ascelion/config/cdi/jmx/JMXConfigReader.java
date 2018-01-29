
package ascelion.config.cdi.jmx;

import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.management.MBeanServer;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;

import static java.util.Collections.emptyMap;
import static java.util.Collections.synchronizedMap;

import org.eclipse.microprofile.config.Config;

@ConfigReader.Type( value = JMXConfigReader.TYPE )
@ApplicationScoped
public class JMXConfigReader implements ConfigReader
{

	static public final String TYPE = "JMX";

	private final Map<String, JMXTree> maps = synchronizedMap( new TreeMap<>() );

	@Inject
	private Instance<Config> cfi;
	@Inject
	private Instance<MBeanServer> mbsi;

	@Override
	public boolean isModified( String source )
	{
		if( this.mbsi.isUnsatisfied() ) {
			return false;
		}

		return this.maps.computeIfAbsent( source, this::buildTree ).isChanged();
	}

	@Override
	public Map<String, String> readConfiguration( String source ) throws ConfigException
	{
		if( this.mbsi.isUnsatisfied() ) {
			return emptyMap();
		}

		return this.maps.computeIfAbsent( source, this::buildTree ).query();
	}

	private JMXTree buildTree( String domain )
	{
		return new JMXTree( domain, this.mbsi.get(), this.cfi.get() );
	}

}
