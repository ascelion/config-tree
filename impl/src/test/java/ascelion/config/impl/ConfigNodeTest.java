
package ascelion.config.impl;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigParseException;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ConfigNodeTest
{

	private final ConfigNodeImpl root = new ConfigNodeImpl();

	@Test
	public void itemOnly()
	{

		this.root.setValue( "a.b.c1.d1", "abcd11" );
		System.out.println( this.root );
		this.root.setValue( "a.b.c2.d2", "abcd22" );
		System.out.println( this.root );
		this.root.setValue( "a.b.c", singletonMap( "d", singletonMap( "e1", "abcde1" ) ) );
		System.out.println( this.root );
		this.root.setValue( "a.b.c", singletonMap( "d.e2", "abcde2" ) );
		System.out.println( this.root );

		try {
			this.root.getNode( "x.y.z" ).getValue();
			fail( "x.y.z" );
		}
		catch( final ConfigNotFoundException e ) {
			;
		}
		assertThat( this.root.getNode( "a.b.c1.d1" ).getValue(), is( "abcd11" ) );
		assertThat( this.root.getNode( "a.b.c2.d2" ).getValue(), is( "abcd22" ) );

		this.root.setValue( "a.b", "ab" );
		System.out.println( this.root );

		assertThat( this.root.getNode( "a.b" ).getValue(), is( "ab" ) );

		final ConfigNode m = this.root.getNode( "a.b" );

		assertThat( m, is( notNullValue() ) );
		assertThat( m.getNode( "c.d.e1" ).getValue(), is( "abcde1" ) );
		assertThat( m.getNode( "c.d.e2" ).getValue(), is( "abcde2" ) );
	}

	@Test
	public void expr()
	{
		this.root.setValue( "item1", "${item2}" );
		this.root.setValue( "item2", "value" );
		this.root.setValue( "item12", "${item1}-${item2}" );
		this.root.setValue( "def1", "${undefined:def_val}" );
		this.root.setValue( "def2", "${undefined:${def1}}" );

		final String v1 = this.root.getNode( "item1" ).getValue();
		final String v2 = this.root.getNode( "item2" ).getValue();
		final String v12 = this.root.getNode( "item12" ).getValue();
		final String d1 = this.root.getNode( "def1" ).getValue();
		final String d2 = this.root.getNode( "def2" ).getValue();

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

	@Test( expected = IllegalArgumentException.class )
	public void pathInvalid()
	{
		this.root.setValue( "prefix-${prop}-suffix", "" );
	}

	@Test
	public void sys()
	{
		System.getProperties().forEach( ( k, v ) -> {
			this.root.setValue( (String) k, ( (String) v ).replace( ":", "\\:" ) );
		} );

		System.getProperties().keySet().stream()
			.sorted()
			.forEach( k -> {
				final String p = (String) k;
				String v = System.getProperty( p );
				final String o = this.root.getNode( p ).getValue();

				if( v.isEmpty() ) {
					v = null;
				}

				assertThat( p, o, is( (Object) v ) );
			} );
	}

	@Test
	public void map()
	{
		this.root.setValue( singletonMap( "a.b", "ab" ) );

		assertThat( this.root.getNode( "a.b" ).getValue(), is( "ab" ) );
	}

	@Test( expected = ConfigException.class )
	public void loop1()
	{
		this.root.setValue( "prop", "${prop}" );

		try {
			this.root.getNode( "prop" ).getValue();
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = ConfigException.class )
	public void loop2()
	{
		this.root.setValue( "prop", "1-${value1}-${value2}-2" );
		this.root.setValue( "value1", "${value}-1" );
		this.root.setValue( "value2", "${value}-2" );
		this.root.setValue( "value", "${prop}" );

		try {
			this.root.getNode( "prop" ).getValue();
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = ConfigException.class )
	public void loop3()
	{
		this.root.setValue( "prop", "${prop1:${prop2:${prop3:${prop}}}}" );

		try {
			this.root.getNode( "prop" ).getValue();
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test
	public void notLoop()
	{
		this.root.setValue( "prop", "1-${value1}-${value2}-2" );
		this.root.setValue( "value1", "${value}-1" );
		this.root.setValue( "value2", "${value}-2" );
		this.root.setValue( "value", "xxx" );

		try {
			this.root.getNode( "prop" ).getValue();
		}
		catch( final Exception e ) {
			e.printStackTrace();

			throw e;
		}
	}

	@Test
	public void run1()
	{
		this.root.setValue( "prop", "value" );

		final String x = this.root.getNode( "prop" ).getValue();

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run2()
	{
		this.root.setValue( "prop", "value" );

		final String x = this.root.getNode( "${prop}" ).getValue();

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run3()
	{
		this.root.setValue( "prop1", "value1-${prop2}" );
		this.root.setValue( "prop2", "value2" );

		final String x = this.root.getNode( "prefix-${prop1}-suffix" ).getValue();

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void run4()
	{
		this.root.setValue( "prop1", "value1-${prop2:value2}" );

		final String x = this.root.getNode( "prefix-${prop1}-suffix" ).getValue();

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void sys1()
	{
		final String x = this.root.getNode( "java.version" ).getValue();

		assertThat( x, is( nullValue() ) );
	}

	@Test
	public void sys2()
	{
		final String x = this.root.getNode( "${java.version}" ).getValue();

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test( expected = ConfigParseException.class )
	public void sys3()
	{
		final String x = this.root.getNode( "version:${java.version}" ).getValue();

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void sys4()
	{
		final String x = this.root.getNode( "${version:${java.version}}" ).getValue();

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test( expected = ConfigParseException.class )
	public void run1Def()
	{
		final String x = this.root.getNode( "prop:default" ).getValue();

		assertThat( x, is( "default" ) );
	}

	@Test
	public void run2Def()
	{
		final String x = this.root.getNode( "${prop:default}" ).getValue();

		assertThat( x, is( "default" ) );
	}
}
