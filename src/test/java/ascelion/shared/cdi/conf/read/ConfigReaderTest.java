
package ascelion.shared.cdi.conf.read;

import java.io.IOException;
import java.io.InputStream;

import ascelion.shared.cdi.conf.ConfigStore;
import ascelion.shared.cdi.conf.read.INIConfigReader;
import ascelion.shared.cdi.conf.read.PRPConfigReader;
import ascelion.shared.cdi.conf.read.XMLConfigReader;
import ascelion.shared.cdi.conf.read.YMLConfigReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigReaderTest
{

	@Test
	public void ini() throws IOException
	{
		try( final InputStream is = getClass().getResourceAsStream( "/config.ini" ) ) {
			assertThat( is, is( notNullValue() ) );

			final INIConfigReader rd = new INIConfigReader();

			rd.readConfiguration( is );

			verify( rd );
		}
	}

	@Test
	public void properties() throws IOException
	{
		try( final InputStream is = getClass().getResourceAsStream( "/config.properties" ) ) {
			assertThat( is, is( notNullValue() ) );

			final PRPConfigReader rd = new PRPConfigReader();

			rd.readConfiguration( is );

			verify( rd );
		}
	}

	@Test
	public void xml() throws IOException
	{
		try( final InputStream is = getClass().getResourceAsStream( "/config.xml" ) ) {
			assertThat( is, is( notNullValue() ) );

			final XMLConfigReader rd = new XMLConfigReader();

			rd.readConfiguration( is );

			verify( rd );
		}
	}

	@Test
	public void yml() throws IOException
	{
		try( final InputStream is = getClass().getResourceAsStream( "/config.yml" ) ) {
			assertThat( is, is( notNullValue() ) );

			final YMLConfigReader rd = new YMLConfigReader();

			rd.readConfiguration( is );

			verify( rd );
		}
	}

	private void verify( final ConfigStore sto )
	{
//		assertThat( sto.getValue( "default" ), is( "0" ) );
//		assertThat( sto.getValue( "prop1" ), is( "value1" ) );
//		assertThat( sto.getValue( "prop2" ), is( "value2" ) );
//		assertThat( sto.getValue( "map1.prop1" ), is( "value1" ) );
//		assertThat( sto.getValue( "map1.prop2" ), is( "value2" ) );
//		assertThat( sto.getValue( "map2.prop1" ), is( "value1" ) );
//		assertThat( sto.getValue( "map2.prop2" ), is( "value2" ) );
//		assertThat( sto.getValue( "map3.prop1" ), is( "value1" ) );
//		assertThat( sto.getValue( "map3.prop2" ), is( "value2" ) );
	}

}
