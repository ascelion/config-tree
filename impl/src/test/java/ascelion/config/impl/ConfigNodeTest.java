
package ascelion.config.impl;

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
		final ConfigNodeImpl cn = new ConfigNodeImpl();

		cn.set( "a.b.c1.d1", "abcd11" );
		System.out.println( cn );
		cn.set( "a.b.c2.d2", "abcd22" );
		System.out.println( cn );
		cn.set( "a.b.c", singletonMap( "d", singletonMap( "e1", "abcde1" ) ) );
		System.out.println( cn );
		cn.set( "a.b.c", singletonMap( "d.e2", "abcde2" ) );
		System.out.println( cn );

		assertThat( cn.getValue( "x.y.z" ), is( nullValue() ) );
		assertThat( cn.getValue( "a.b.c1.d1" ), is( "abcd11" ) );
		assertThat( cn.getValue( "a.b.c2.d2" ), is( "abcd22" ) );

		cn.set( "a.b", "ab" );
		System.out.println( cn );

		assertThat( cn.getValue( "a.b" ), is( "ab" ) );

		final ConfigNodeImpl m = cn.getNode( "a.b" );

		assertThat( m, is( notNullValue() ) );
		assertThat( m.getValue( "c.d.e1" ), is( "abcde1" ) );
		assertThat( m.getValue( "c.d.e2" ), is( "abcde2" ) );
	}

	@Test
	public void sys()
	{
		final ConfigNodeImpl cn = new ConfigNodeImpl();

		System.getProperties().forEach( ( k, v ) -> cn.set( (String) k, v ) );

		System.getProperties().keySet().stream()
			.sorted()
			.forEach( k -> {
				final String p = (String) k;
				final String v = System.getProperty( p );
				final String o = cn.getValue( p );

				assertThat( p, o, is( (Object) v ) );
			} );
	}

	@Test
	public void xyz()
	{
		final ConfigNodeImpl cn = new ConfigNodeImpl();

		cn.set( null, singletonMap( "a.b", "ab" ) );

		assertThat( cn.getValue( "a.b" ), is( "ab" ) );
	}

}
