
package ascelion.config.read;

import java.util.Map;

import ascelion.config.api.ConfigReader;
import ascelion.config.eclipse.ext.ConfigChangeListener;
import ascelion.config.eclipse.ext.ConfigChangeListenerSupport;
import ascelion.config.eclipse.ext.ConfigSourceExt;
import ascelion.logging.LOG;

import static java.lang.String.format;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode( of = { "source", "type" }, doNotUseGetters = true )
final class AnnotatedConfigSource implements ConfigSourceExt
{

	static private final LOG L = LOG.get();

	private final ConfigReader rd;
	private Map<String, String> properties;
	private final String source;
	private final String type;
	private final int priority;
	private final ConfigChangeListenerSupport cls = new ConfigChangeListenerSupport( this );

	AnnotatedConfigSource( ConfigReader rd, String type, String source, int priority )
	{
		this.rd = rd;
		this.type = type;
		this.source = source;
		this.priority = priority;
	}

	@Override
	public String toString()
	{
		return format( "@%s(%s) -> \"%s\"", this.type, this.priority, this.source );
	}

	@Override
	public synchronized Map<String, String> getProperties()
	{
		if( this.properties == null || this.rd.isModified( this.source ) ) {
			this.properties = this.rd.readConfiguration( this.source );

			this.cls.fireChanged();
		}

		return this.properties;
	}

	@Override
	public String getValue( String propertyName )
	{
		return getProperties().get( propertyName );
	}

	@Override
	public String getName()
	{
		return this.source;
	}

	@Override
	public int getOrdinal()
	{
		return this.priority;
	}

	@Override
	public void addChangeListener( ConfigChangeListener cl )
	{
		this.cls.add( cl );
	}

	@Override
	public void removeChangeListener( ConfigChangeListener cl )
	{
		this.cls.remove( cl );
	}
}
