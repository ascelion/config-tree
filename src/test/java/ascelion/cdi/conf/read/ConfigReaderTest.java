
package ascelion.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;

import ascelion.cdi.conf.ConfigNodeImpl;
import ascelion.cdi.conf.read.INIConfigReader;
import ascelion.cdi.conf.read.PRPConfigReader;
import ascelion.cdi.conf.read.XMLConfigReader;
import ascelion.cdi.conf.read.YMLConfigReader;
import ascelion.shared.cdi.conf.ConfigReader;
import ascelion.shared.cdi.conf.ConfigSource;
import ascelion.shared.cdi.conf.ConfigSource.Type;

import static java.lang.String.format;
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

	@Test
	public void run() throws InstantiationException, IllegalAccessException, IOException
	{
		final Type a = this.cls.getAnnotation( ConfigSource.Type.class );

		assertThat( a, is( notNullValue() ) );

		final String res = format( "/config.%s", a.value().toLowerCase() );
		final InputStream source = getClass().getResourceAsStream( res );

		assertThat( source, is( notNullValue() ) );

		final ConfigReader rd = this.cls.newInstance();
		final ConfigNodeImpl cn = new ConfigNodeImpl();

		rd.readConfiguration( cn, source );

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
