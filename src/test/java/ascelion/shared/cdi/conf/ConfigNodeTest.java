
package ascelion.shared.cdi.conf;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigNodeTest
{

	@Test
	public void run()
	{
		final ConfigNode cn = new ConfigNode();

		cn.set( "a.b.c1.d1", "abcd11" );
		cn.set( "a.b.c2.d2", "abcd22" );
		cn.set( "a.b.c", singletonMap( "d", new ConfigNode( "e1" ).set( "abcde1" ) ) );
		cn.set( "a.b.c", singletonMap( "d.e2", "abcde2" ) );

		assertThat( cn.getItem( "x.y.z" ), is( nullValue() ) );
		assertThat( cn.getItem( "a.b.c1.d1" ), is( "abcd11" ) );
		assertThat( cn.getItem( "a.b.c2.d2" ), is( "abcd22" ) );

		final ConfigNode m = cn.getNode( "a.b" );

		assertThat( m, is( notNullValue() ) );
		assertThat( m.getItem( "c.d.e1" ), is( "abcde1" ) );
		assertThat( m.getItem( "c.d.e2" ), is( "abcde2" ) );
	}

	@Test
	public void sys()
	{
		final ConfigNode cn = new ConfigNode();

		System.getProperties().forEach( ( k, v ) -> cn.set( (String) k, v ) );

		System.getProperties().keySet().stream()
			.sorted()
			.forEach( k -> {
				final String p = (String) k;
				final String v = System.getProperty( p );
				final String o = cn.getItem( p );

				assertThat( p, o, is( (Object) v ) );
			} );
	}

}
