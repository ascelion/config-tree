
package ascelion.shared.cdi.conf;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigStoreTest
{

	@Test
	public void setValue()
	{
		final ConfigStore cm = new ConfigStore();

		cm.setValue( "a.b.c.d", "abcd" );

		assertThat( cm.getValue( "a.b.c.d" ).getItem(), is( "abcd" ) );

		final Map<String, ? extends ConfigItem> m = cm.getValue( "a.b.c" ).getTree();

		assertThat( m, is( notNullValue() ) );

		final ConfigItem i = m.get( "d" );

		assertThat( i, is( notNullValue() ) );
		assertThat( i.getItem(), is( "abcd" ) );
	}

	@Test
	public void add()
	{
		final ConfigStore cm1 = new ConfigStore();
		final ConfigStore cm2 = new ConfigStore();

		cm1.setValue( "a.b.c.d", "abcd" );
		cm2.setValue( "a.b.c.e", "abce" );

		cm1.add( cm2.get() );

		assertThat( cm1.getValue( "a.b.c.d" ).getItem(), is( "abcd" ) );
		assertThat( cm1.getValue( "a.b.c.d" ).getItem(), is( "abcd" ) );

		final Map<String, ? extends ConfigItem> m = cm1.getValue( "a.b.c" ).getTree();

		assertThat( m.get( "d" ).getItem(), is( "abcd" ) );
		assertThat( m.get( "e" ).getItem(), is( "abce" ) );
	}

}
