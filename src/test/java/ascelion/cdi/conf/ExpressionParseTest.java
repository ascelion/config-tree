
package ascelion.cdi.conf;

import java.util.LinkedHashSet;

import ascelion.cdi.conf.ExpressionRules.Rule;
import ascelion.shared.cdi.conf.ConfigNode;

import static ascelion.cdi.conf.ExpressionRules.parse;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ExpressionParseTest
{

	private final ConfigNode root = new ConfigNodeImpl();

	@Test( expected = ExpressionException.class )
	public void asItemLoop()
	{
		this.root.setValue( "root", "1-${value1}-${value2}-2" );
		this.root.setValue( "value1", "${value}-1" );
		this.root.setValue( "value2", "${value}-2" );
		this.root.setValue( "value", "${root}" );

		final Rule rule = parse( "root" );

		rule.evaluate( this.root, new LinkedHashSet<>() );
	}

	@Test
	public void run1()
	{
		this.root.setValue( "root.prop1", "value" );

		final Rule rule = parse( "root.prop1" );
		final String x = rule.evaluate( this.root, new LinkedHashSet<>() );

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run2()
	{
		this.root.setValue( "root.prop1", "value" );

		final Rule rule = parse( "${root.prop1}" );
		final String x = rule.evaluate( this.root, new LinkedHashSet<>() );

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run3()
	{
		this.root.setValue( "prop1", "value1-${prop2}" );
		this.root.setValue( "prop2", "value2" );

		final Rule rule = parse( "prefix-${prop1}-suffix" );
		final String x = rule.evaluate( this.root, new LinkedHashSet<>() );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void run4()
	{
		this.root.setValue( "prop1", "value1-${prop2:value2}" );

		final Rule rule = parse( "prefix-${prop1}-suffix" );
		final String x = rule.evaluate( this.root, new LinkedHashSet<>() );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void run1Def()
	{
		final Rule rule = parse( "prop:default" );
		final String x = rule.evaluate( this.root, new LinkedHashSet<>() );

		assertThat( x, is( "default" ) );
	}

	@Test
	public void run2Def()
	{
		final Rule rule = parse( "${prop:default}" );
		final String x = rule.evaluate( this.root, new LinkedHashSet<>() );

		assertThat( x, is( "default" ) );
	}
}
