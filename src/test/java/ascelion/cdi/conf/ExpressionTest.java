
package ascelion.cdi.conf;

import java.util.List;

import ascelion.cdi.conf.ConfigNodeImpl;
import ascelion.cdi.conf.Expression;
import ascelion.cdi.conf.ExpressionItem;
import ascelion.shared.cdi.conf.ConfigNode;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ExpressionTest
{

	@Test( expected = IllegalArgumentException.class )
	public void asItemLoop()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "root", "1-${value1}-${value2}-2" );
		root.setValue( "value1", "${value}-1" );
		root.setValue( "value2", "${value}-2" );
		root.setValue( "value", "${root}" );

		final Expression exp = new Expression( "root" );

		exp.asItem( root );
	}

	@Test
	public void items()
	{
		List<ExpressionItem> items = ExpressionItem.items( "${${${x}}}" );

		for( int k = 0; k < 3; k++ ) {
			assertThat( items, hasSize( 1 ) );

			final ExpressionItem i = items.get( 0 );

			items = ExpressionItem.items( i.v );
		}

		assertThat( items, is( nullValue() ) );
	}

	@Test
	public void defItem()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "def", "default" );

		assertThat( new Expression( "x:${def}" ).asItem( root ), is( "default" ) );
		assertThat( new Expression( "x:${${def}}" ).asItem( root ), is( "default" ) );
		assertThat( new Expression( "${x:${def}}" ).asItem( root ), is( "default" ) );
		assertThat( new Expression( "${${x:${def}}}" ).asItem( root ), is( "default" ) );
		assertThat( new Expression( "${${x:${${def}}}}" ).asItem( root ), is( "default" ) );
	}

	@Test
	public void asItem1()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "root", "1-${value1}-${value2}-2" );
		root.setValue( "value1", "${val1}-1" );
		root.setValue( "value2", "${val2}-2" );
		root.setValue( "val1", "v" );
		root.setValue( "val2", "valval(${val})" );

		final String val = new Expression( "root" ).asItem( root );

		assertThat( val, is( "1-v-1-valval(${val})-2-2" ) );
	}

	@Test
	public void asItem2()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "p1", "1" );
		root.setValue( "p2", "2" );
		root.setValue( "p3", "3" );
		root.setValue( "pv", "[${p1}, ${p2}, ${p3}]" );

		final Expression exp = new Expression( "${pv}" );
		final String val = exp.asItem( root );

		assertThat( val, is( "[1, 2, 3]" ) );
	}

	@Test
	public void asItem3()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "p1", "100" );
		root.setValue( "p2", "200" );
		root.setValue( "p3", "300" );
		root.setValue( "pv", "[${p1}, ${p2}, ${p3}]" );

		final Expression exp = new Expression( "pv" );
		final String val = exp.asItem( root );

		assertThat( val, is( "[100, 200, 300]" ) );
	}

	@Test
	public void asItem4()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "A", "1" );
		root.setValue( "B", "2" );
		root.setValue( "C", "3" );
		root.setValue( "D", "${A}-${B}-${C}" );

		final Expression exp = new Expression( "D" );
		final String val = exp.asItem( root );

		assertThat( val, is( "1-2-3" ) );
	}

	@Test
	public void asItem5()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "version", "${java.version}" );

		final Expression exp = new Expression( "${version}" );

		final String val1 = exp.asItem( root );

		assertThat( val1, is( System.getProperty( "java.version" ) ) );

		root.setValue( "java.version", "some.version" );

		final String val2 = exp.asItem( root );

		assertThat( val2, is( "some.version" ) );
	}

	@Test
	public void asNode1()
	{
		final ConfigNode root = new ConfigNodeImpl();

		root.setValue( "root.map-1.A", "1" );
		root.setValue( "root.map-1.B", "2" );
		root.setValue( "root.map-1.C", "1, 2, 3" );

		root.setValue( "root.child.map", "${map.path}-${map.index}" );
		root.setValue( "map.path", "root.map" );
		root.setValue( "map.index", "1" );

		root.setValue( "ref", "${root.child.map}" );

		final ConfigNode map1 = new Expression( "ref" ).asNode( root );
		final ConfigNode map2 = new Expression( "${ref}" ).asNode( root );
		final ConfigNode map3 = new Expression( "x:${ref}" ).asNode( root );
		final ConfigNode map4 = new Expression( "${x:ref}" ).asNode( root );

		assertThat( map1, is( notNullValue() ) );
		assertThat( map2, is( notNullValue() ) );
		assertThat( map3, is( notNullValue() ) );
		assertThat( map4, is( notNullValue() ) );
	}
}
