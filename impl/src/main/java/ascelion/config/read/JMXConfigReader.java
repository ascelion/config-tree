
package ascelion.config.read;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigReader;
import ascelion.config.impl.ConfigBean;

import static java.lang.String.format;

@ConfigReader.Type( value = JMXConfigReader.TYPE )
public class JMXConfigReader implements ConfigReader
{

	static public final String TYPE = "JMX";

	@Inject
	private Instance<MBeanServer> mbsi;

	private MBeanServer mbs;
	private boolean modified;

	@Override
	public boolean isModified( String source )
	{
		return this.mbs == null || this.modified;
	}

	@Override
	public Map<String, String> readConfiguration( String source )
	{
		final Map<String, String> map = new HashMap<>();

		if( enabled() ) {
			try {
				this.mbs.queryNames( new ObjectName( source + ":*" ), null )
					.forEach( name -> putValue( map, name ) );
				;
			}
			catch( final MalformedObjectNameException e ) {
				throw new ConfigException( format( "Cannot get domain %s", source ) );
			}
		}

		return map;
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

	public void setMBeanServer( MBeanServer mbs )
	{
		this.mbs = mbs;
	}

	private void putValue( Map<String, String> map, ObjectName name )
	{
		final ConfigBean cb = JMX.newMXBeanProxy( this.mbs, name, ConfigBean.class );

		map.put( cb.getPath(), cb.getRawValue() );
	}

}
