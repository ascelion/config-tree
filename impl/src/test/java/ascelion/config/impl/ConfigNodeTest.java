
package ascelion.config.impl;

import ascelion.config.api.ConfigException;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConfigNodeTest
{

	private final ConfigNodeImpl root = new ConfigNodeImpl();

	@Test
	public void itemOnly()
	{

		this.root.set( "a.b.c1.d1", "abcd11" );
		System.out.println( this.root );
		this.root.set( "a.b.c2.d2", "abcd22" );
		System.out.println( this.root );
		this.root.set( "a.b.c", singletonMap( "d", singletonMap( "e1", "abcde1" ) ) );
		System.out.println( this.root );
		this.root.set( "a.b.c", singletonMap( "d.e2", "abcde2" ) );
		System.out.println( this.root );

		assertThat( this.root.getValue( "x.y.z" ), is( nullValue() ) );
		assertThat( this.root.getValue( "a.b.c1.d1" ), is( "abcd11" ) );
		assertThat( this.root.getValue( "a.b.c2.d2" ), is( "abcd22" ) );

		this.root.set( "a.b", "ab" );
		System.out.println( this.root );

		assertThat( this.root.getValue( "a.b" ), is( "ab" ) );

		final ConfigNodeImpl m = this.root.getNode( "a.b" );

		assertThat( m, is( notNullValue() ) );
		assertThat( m.getValue( "c.d.e1" ), is( "abcde1" ) );
		assertThat( m.getValue( "c.d.e2" ), is( "abcde2" ) );
	}

	@Test
	public void expr()
	{
		this.root.set( "item1", "${item2}" );
		this.root.set( "item2", "value" );
		this.root.set( "item12", "${item1}-${item2}" );
		this.root.set( "def1", "${undefined:def_val}" );
		this.root.set( "def2", "${undefined:${def1}}" );

		final String v1 = this.root.getValue( "item1" );
		final String v2 = this.root.getValue( "item2" );
		final String v12 = this.root.getValue( "item12" );
		final String d1 = this.root.getValue( "def1" );
		final String d2 = this.root.getValue( "def2" );

		System.out.printf( "item1 = %s\n", v1 );
		System.out.printf( "item2 = %s\n", v2 );
		System.out.printf( "item12 = %s\n", v12 );
		System.out.printf( "def1 = %s\n", d1 );
		System.out.printf( "def2 = %s\n", d2 );

		assertThat( v1, is( "value" ) );
		assertThat( v2, is( "value" ) );
		assertThat( v12, is( "value-value" ) );
		assertThat( d1, is( "def_val" ) );
		assertThat( d2, is( "def_val" ) );
	}

	@Test
	public void sys()
	{
		System.getProperties().forEach( ( k, v ) -> this.root.set( (String) k, v.toString().replaceAll( ":", "\\:" ) ) );

		System.getProperties().keySet().stream()
			.sorted()
			.forEach( k -> {
				final String p = (String) k;
				final String v = System.getProperty( p );
				final String o = this.root.getValue( p );

				assertThat( p, o, is( (Object) v ) );
			} );
	}

	@Test
	public void map()
	{
		this.root.set( null, singletonMap( "a.b", "ab" ) );

		assertThat( this.root.getValue( "a.b" ), is( "ab" ) );
	}

	@Test( expected = ConfigException.class )
	public void loop1()
	{
		this.root.set( "prop", "${prop}" );

		try {
			this.root.getValue( "prop" );
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = ConfigException.class )
	public void loop2()
	{
		this.root.set( "prop", "1-${value1}-${value2}-2" );
		this.root.set( "value1", "${value}-1" );
		this.root.set( "value2", "${value}-2" );
		this.root.set( "value", "${prop}" );

		try {
			this.root.getValue( "prop" );
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = ConfigException.class )
	public void loop3()
	{
		this.root.set( "prop", "${prop1:${prop2:${prop3:${prop}}}}" );

		try {
			this.root.getValue( "prop" );
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test
	public void notLoop()
	{
		this.root.set( "prop", "1-${value1}-${value2}-2" );
		this.root.set( "value1", "${value}-1" );
		this.root.set( "value2", "${value}-2" );
		this.root.set( "value", "xxx" );

		try {
			this.root.getValue( "prop" );
		}
		catch( final Exception e ) {
			e.printStackTrace();

			throw e;
		}
	}

	@Test
	public void run1()
	{
		this.root.set( "prop", "value" );

		final String x = this.root.getValue( "prop" );

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run2()
	{
		this.root.set( "prop", "value" );

		final String x = this.root.getValue( "${prop}" );

		assertThat( x, is( nullValue() ) );
	}

	@Test
	public void run3()
	{
		this.root.set( "prop1", "value1-${prop2}" );
		this.root.set( "prop2", "value2" );
		this.root.set( "prop3", "prefix-${prop1}-suffix" );

		final String x = this.root.getValue( "prop3" );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void run4()
	{
		this.root.set( "prop1", "value1-${prop2:value2}" );

		final String x = this.root.getValue( "prefix-${prop1}-suffix" );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void sys1()
	{
		final String x = this.root.getValue( "java.version" );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void sys2()
	{
		final String x = this.root.getValue( "${java.version}" );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void sys3()
	{
		final String x = this.root.getValue( "version:${java.version}" );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void sys4()
	{
		final String x = this.root.getValue( "${version:${java.version}}" );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void run1Def()
	{
		final String x = this.root.getValue( "prop:default" );

		assertThat( x, is( "default" ) );
	}

	@Test
	public void run2Def()
	{
		final String x = this.root.getValue( "${prop:default}" );

		assertThat( x, is( "default" ) );
	}
}
