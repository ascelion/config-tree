
package ascelion.shared.cdi.conf;

import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigStoreTest
{

	@Test
	public void set()
	{
		final ConfigStore cm = new ConfigStore();

		cm.setValue( "a.b.c.d", "abcd" );

		assertThat( cm.getValue( "a.b.c.d" ), is( "abcd" ) );
		assertThat( cm.getValue( "a.b.c" ), is( instanceOf( Map.class ) ) );
	}

}
