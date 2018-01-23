
package ascelion.config.read;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.ConfigLoad;
import ascelion.config.impl.ConfigSourceLiteral;

import static ascelion.config.impl.Utils.asArray;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ResourceReaderTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			asArray( INIConfigReader.class ),
			asArray( PRPConfigReader.class ),
			asArray( XMLConfigReader.class ),
			asArray( YMLConfigReader.class ),
		};
	}

	@Parameterized.Parameter( 0 )
	public Class<? extends ResourceReader> cls;

	@ConfigReader.Type( "STREAM" )
	static class ConfigStreamReader implements ConfigReader
	{

		final InputStream stream;
		final ResourceReader delegate;

		ConfigStreamReader( InputStream stream, ResourceReader delegate )
		{
			this.stream = stream;
			this.delegate = delegate;
		}

		@Override
		public Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException
		{
			final Map<String, Object> map = new TreeMap<>();
			try {
				this.delegate.readConfiguration( map, this.stream );
			}
			catch( final IOException e ) {
				throw new ConfigException( e.getMessage() );
			}

			return map;
		}
	}

	@Test
	public void run() throws InstantiationException, IllegalAccessException, IOException
	{
		final ConfigReader.Type a = this.cls.getAnnotation( ConfigReader.Type.class );

		assertThat( a, is( notNullValue() ) );

		InputStream source = input( a.value() );

		if( source == null ) {
			for( final String type : a.types() ) {
				source = input( type );

				if( source != null ) {
					break;
				}
			}
		}
		assertThat( source, is( notNullValue() ) );

		final ConfigLoad ld = new ConfigLoad();
		final ConfigReader rd = new ConfigStreamReader( source, this.cls.newInstance() );

		ld.addReader( rd );
		ld.addSource( new ConfigSourceLiteral( "", 0, "STREAM" ) );

		final ConfigNode cn = ld.load();

		assertThat( cn.getNode( "default" ).getValue(), is( "0" ) );
		assertThat( cn.getNode( "prop1" ).getValue(), is( "value1" ) );
		assertThat( cn.getNode( "prop2" ).getValue(), is( "value2" ) );
		assertThat( cn.getNode( "map1.prop1" ).getValue(), is( "value1" ) );
		assertThat( cn.getNode( "map1.prop2" ).getValue(), is( "value2" ) );
		assertThat( cn.getNode( "map2.prop1" ).getValue(), is( "value1" ) );
		assertThat( cn.getNode( "map2.prop2" ).getValue(), is( "value2" ) );
		assertThat( cn.getNode( "map3.prop1" ).getValue(), is( "value1" ) );
		assertThat( cn.getNode( "map3.prop2" ).getValue(), is( "value2" ) );
	}

	private InputStream input( String type )
	{
		final String res = format( "/config.%s", type.toLowerCase() );
		final InputStream source = getClass().getResourceAsStream( res );

		return source;
	}
}