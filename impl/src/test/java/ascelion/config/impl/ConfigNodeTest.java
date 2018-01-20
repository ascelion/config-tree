
package ascelion.config.impl;

import java.util.Collection;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ConfigNodeTest
{

	private final ConfigNodeImpl root = new ConfigNodeImpl();

	@Test
	public void both()
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
			this.root.getValue( "x.y.z" );
			fail( "x.y.z" );
		}
		catch( final ConfigNotFoundException e ) {
			;
		}
		assertThat( this.root.getValue( "a.b.c1.d1" ), is( "abcd11" ) );
		assertThat( this.root.getValue( "a.b.c2.d2" ), is( "abcd22" ) );

		final ConfigNode m = this.root.getNode( "a.b" );

		assertThat( m, is( notNullValue() ) );
		assertThat( m.getValue( "c.d.e1" ), is( "abcde1" ) );
		assertThat( m.getValue( "c.d.e2" ), is( "abcde2" ) );
	}

	@Test
	public void values()
	{
		this.root.setValue( "item1", "${item2}" );
		this.root.setValue( "item2", "value" );
		this.root.setValue( "item12", "${item1}-${item2}" );
		this.root.setValue( "def1", "${undefined:-def_val}" );
		this.root.setValue( "def2", "${undefined:-${def1}}" );

		final String v1 = this.root.getValue( "item1" );
		final String v2 = this.root.getValue( "item2" );
		final String v12 = this.root.getValue( "item12" );
		final String d1 = this.root.getValue( "def1" );
		final String d2 = this.root.getValue( "def2" );
		final String d3 = this.root.getValue( "${undefined:-def_val_2}" );

		System.out.printf( "item1 = %s\n", v1 );
		System.out.printf( "item2 = %s\n", v2 );
		System.out.printf( "item12 = %s\n", v12 );
		System.out.printf( "def1 = %s\n", d1 );
		System.out.printf( "def2 = %s\n", d2 );
		System.out.printf( "def2 = %s\n", d3 );
		System.out.printf( "def2 = %s\n", d3 );

		assertThat( v1, is( "value" ) );
		assertThat( v2, is( "value" ) );
		assertThat( v12, is( "value-value" ) );
		assertThat( d1, is( "def_val" ) );
		assertThat( d2, is( "def_val" ) );
		assertThat( d3, is( "def_val_2" ) );
	}

	@Test
	public void nodes()
	{
		this.root.setValue( "item1", "${item2}" );
		this.root.setValue( "item2", "value" );
		this.root.setValue( "item12", "${item1}-${item2}" );
		this.root.setValue( "def1", "${undefined:-def_val}" );
		this.root.setValue( "def2", "${undefined:-${def1}}" );

		final ConfigNode v1 = this.root.getNode( "item1" );
		final ConfigNode v2 = this.root.getNode( "item2" );
		final ConfigNode v12 = this.root.getNode( "item12" );
		final ConfigNode d1 = this.root.getNode( "def1" );
		final ConfigNode d2 = this.root.getNode( "def2" );

		try {
			this.root.getNode( "undefined:-def_val_2" );
			fail( "expecting not found" );
		}
		catch( final ConfigNotFoundException e ) {
			;
		}
		try {
			this.root.getNode( "${undefined:-def_val_2}" );
			fail( "expecting not found" );
		}
		catch( final ConfigNotFoundException e ) {
			;
		}

		System.out.printf( "item1 = %s\n", v1 );
		System.out.printf( "item2 = %s\n", v2 );
		System.out.printf( "item12 = %s\n", v12 );
		System.out.printf( "def1 = %s\n", d1 );
		System.out.printf( "def2 = %s\n", d2 );

		assertThat( v1.getValue(), is( "value" ) );
		assertThat( v2.getValue(), is( "value" ) );
		assertThat( v12.getValue(), is( "value-value" ) );
		assertThat( d1.getValue(), is( "def_val" ) );
		assertThat( d2.getValue(), is( "def_val" ) );
	}

	@Test
	public void keys()
	{
		this.root.setValue( "prop", "prop" );

		assertThat( this.root.getKeys(), contains( "prop" ) );
	}

	@Test
	public void sys()
	{
		System.getProperties().forEach( ( k, v ) -> {
			this.root.setValue( (String) k, ( (String) v ) );
		} );

		this.root.getKeys().stream()
			.sorted()
			.forEach( k -> {
				final String o = this.root.getValue( k );
				final String v = System.getProperty( k );

				assertEquals( k, v, o );
			} );
	}

	@Test
	public void map()
	{
		this.root.setValue( singletonMap( "a.b", "ab" ) );

		assertThat( this.root.getValue( "a.b" ), is( "ab" ) );
	}

	@Test( expected = ConfigLoopException.class )
	public void loop1()
	{
		this.root.setValue( "prop", "${prop}" );

		try {
			this.root.getValue( "prop" );
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = ConfigLoopException.class )
	public void loop2()
	{
		this.root.setValue( "prop", "1-${value1}-${value2}-2" );
		this.root.setValue( "value1", "${value}-1" );
		this.root.setValue( "value2", "${value}-2" );
		this.root.setValue( "value", "${prop}" );

		try {
			this.root.getValue( "prop" );
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = ConfigLoopException.class )
	public void loop3()
	{
		this.root.setValue( "prop", "${prop1:${prop2:${prop3:${prop}}}}" );

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
		this.root.setValue( "prop", "1-${value1}-${value2}-2" );
		this.root.setValue( "value1", "${value}-1" );
		this.root.setValue( "value2", "${value}-2" );
		this.root.setValue( "value", "xxx" );

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
		this.root.setValue( "prop", "value" );

		final String x = this.root.getValue( "prop" );

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run2()
	{
		this.root.setValue( "prop", "value" );

		final String x = this.root.getValue( "${prop}" );

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run3()
	{
		this.root.setValue( "prop1", "value1-${prop2}" );
		this.root.setValue( "prop2", "value2" );
		this.root.setValue( "prop3", "prefix-${prop1}-suffix" );

		final String x = this.root.getValue( "prop3" );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void run4()
	{
		this.root.setValue( "prop1", "value1-${prop2:-value2}" );

		final String x = this.root.getValue( "prefix-${prop1}-suffix" );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void run5()
	{
		this.root.setValue( "prop1", "value1-${prop2:-value2}" );

		final String x = this.root.getValue( "${prefix-${prop1}-suffix:-${prop1}}" );

		assertThat( x, is( "value1-value2" ) );
	}

	@Test( expected = ConfigNotFoundException.class )
	public void run6()
	{
		this.root.getValue( "prop" );
	}

	@Test( expected = ConfigNotFoundException.class )
	public void run7()
	{
		this.root.getValue( "${prop}" );
	}

	@Test( expected = ConfigNotFoundException.class )
	public void run8()
	{
		this.root.getValue( "prefix-${prop1}-suffix" );
	}

	@Test( expected = ConfigNotFoundException.class )
	public void run9()
	{
		this.root.setValue( "prop1", "${prop2}" );

		this.root.getValue( "prefix-${prop1}-suffix" );
	}

	@Test
	public void links()
	{
		this.root.setValue( "X.a.1", "a1" );
		this.root.setValue( "X.a.2", "a2" );
		this.root.setValue( "X.b", "${X.a}" );
		this.root.setValue( "X.c", "${X.b}" );

		final ConfigNode a = this.root.getNode( "X.a" );
		final ConfigNode b = this.root.getNode( "X.b" );
		final ConfigNode c = this.root.getNode( "X.c" );

		final Collection<ConfigNode> ca = a.getNodes();
		final Collection<ConfigNode> cb = b.getNodes();
		final Collection<ConfigNode> cc = c.getNodes();

		assertNotNull( ca );
		assertNotNull( cb );
		assertNotNull( cc );

		assertEquals( ca.size(), cb.size() );
	}

	@Test
	public void links2()
	{
		this.root.setValue( "X.a.1", "a1" );
		this.root.setValue( "X.a.2", "a2" );
		this.root.setValue( "X.b", "${X.${B}}" );
		this.root.setValue( "X.c", "${X.${C}}" );
		this.root.setValue( "B", "a" );
		this.root.setValue( "C", "b" );

		final ConfigNode a = this.root.getNode( "X.a" );
		final ConfigNode b = this.root.getNode( "X.b" );
		final ConfigNode c = this.root.getNode( "X.c" );

		final Collection<ConfigNode> ca = a.getNodes();
		final Collection<ConfigNode> cb = b.getNodes();
		final Collection<ConfigNode> cc = c.getNodes();

		assertNotNull( ca );
		assertNotNull( cb );
		assertNotNull( cc );

		assertEquals( ca.size(), cb.size() );
	}

	@Test
	public void links3()
	{
		this.root.setValue( "X.a.1", "a1" );
		this.root.setValue( "X.a.2", "a2" );
		this.root.setValue( "X.b", "X.${B}" );
		this.root.setValue( "X.c", "X.${C}" );
		this.root.setValue( "B", "a" );
		this.root.setValue( "C", "b" );

		final ConfigNode a = this.root.getNode( "X.a" );
		final ConfigNode b = this.root.getNode( "X.b" );
		final ConfigNode c = this.root.getNode( "X.c" );

		final Collection<ConfigNode> ca = a.getNodes();
		final Collection<ConfigNode> cb = b.getNodes();
		final Collection<ConfigNode> cc = c.getNodes();

		assertNotNull( ca );
		assertNotNull( cb );
		assertNotNull( cc );

		assertEquals( ca.size(), cb.size() );
	}

	@Test
	public void runDef()
	{
		final String x = this.root.getValue( "${prop:-default}" );

		assertThat( x, is( "default" ) );
	}
}
