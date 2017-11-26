
package ascelion.config.eclipse;

import java.lang.reflect.Type;
import java.util.Map;

import ascelion.config.api.ConfigReader;
import ascelion.config.conv.Converters;
import ascelion.config.impl.ConfigLoad;
import ascelion.config.impl.ConfigSourceLiteral;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;

import org.eclipse.microprofile.config.spi.ConfigSource;

final class ConfigSourceImpl implements ConfigSource
{

	static private final Type TYPE = parameterizedClass( Map.class, String.class, String.class );

	private final String name;
	private final ConfigLoad ld = new ConfigLoad();
	private Map<String, String> properties;

	ConfigSourceImpl( ConfigReader rd )
	{
		this( rd, "" );
	}

	ConfigSourceImpl( ConfigReader rd, String name )
	{
		this.name = name;

		final ConfigReader.Type rt = rd.getClass().getAnnotation( ConfigReader.Type.class );

		if( rt != null ) {
			this.ld.addSource( new ConfigSourceLiteral( "", 0, rt.value() ) );
		}
		else {
			this.ld.addSource( new ConfigSourceLiteral( "", 0, rd.getClass().getName() ) );
		}

		this.ld.addReader( rd );
	}

	@Override
	public Map<String, String> getProperties()
	{
		return load();
	}

	@Override
	public String getValue( String propertyName )
	{
		return load().get( propertyName );
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	private Map<String, String> load()
	{
		if( this.properties == null ) {
			final Converters cv = new Converters();

			this.properties = (Map<String, String>) cv.create( TYPE, this.ld.load(), 0 );
		}

		return this.properties;
	}

}
