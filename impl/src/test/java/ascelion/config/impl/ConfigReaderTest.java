
package ascelion.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.read.INIConfigReader;
import ascelion.config.impl.read.PRPConfigReader;
import ascelion.config.impl.read.XMLConfigReader;
import ascelion.config.impl.read.YMLConfigReader;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ConfigReaderTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			new Object[] { INIConfigReader.class },
			new Object[] { PRPConfigReader.class },
			new Object[] { XMLConfigReader.class, },
			new Object[] { YMLConfigReader.class },
		};
	}

	@Parameterized.Parameter( 0 )
	public Class<? extends ConfigReader> cls;

	@ConfigReader.Type( "STREAM" )
	static class ConfigStreamReader implements ConfigReader
	{

		final InputStream stream;
		final ConfigReader delegate;

		ConfigStreamReader( InputStream stream, ConfigReader delegate )
		{
			this.stream = stream;
			this.delegate = delegate;
		}

		@Override
		public boolean enabled()
		{
			return this.delegate.enabled();
		}

		@Override
		public Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException
		{
			try {
				return this.delegate.readConfiguration( source, this.stream );
			}
			catch( final IOException e ) {
				throw new ConfigException( e.getMessage() );
			}
		}
	}

	@Test
	public void run() throws InstantiationException, IllegalAccessException, IOException
	{
		final ConfigReader.Type a = this.cls.getAnnotation( ConfigReader.Type.class );

		assertThat( a, is( notNullValue() ) );

		final String res = format( "/config.%s", a.value().toLowerCase() );
		final InputStream source = getClass().getResourceAsStream( res );

		assertThat( source, is( notNullValue() ) );

		final ConfigLoad ld = new ConfigLoad();
		final ConfigReader rd = new ConfigStreamReader( source, this.cls.newInstance() );

		ld.addReader( rd );

		final ConfigNode cn = ld.load( asList( new ConfigSourceLiteral( "", 0, "STREAM" ) ) );

		assertThat( cn.getValue( "default" ), is( "0" ) );
		assertThat( cn.getValue( "prop1" ), is( "value1" ) );
		assertThat( cn.getValue( "prop2" ), is( "value2" ) );
		assertThat( cn.getValue( "map1.prop1" ), is( "value1" ) );
		assertThat( cn.getValue( "map1.prop2" ), is( "value2" ) );
		assertThat( cn.getValue( "map2.prop1" ), is( "value1" ) );
		assertThat( cn.getValue( "map2.prop2" ), is( "value2" ) );
		assertThat( cn.getValue( "map3.prop1" ), is( "value1" ) );
		assertThat( cn.getValue( "map3.prop2" ), is( "value2" ) );
	}

}
