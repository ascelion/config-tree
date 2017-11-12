
package ascelion.config.impl;

import ascelion.config.api.ConfigNode;
import ascelion.config.impl.ConfigNodeImpl;
import ascelion.config.impl.EvalException;

import static ascelion.config.impl.Eval.eval;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EvalTest
{

	private final ConfigNode root = new ConfigNodeImpl();

	@Test( expected = EvalException.class )
	public void loop1()
	{
		this.root.setValue( "prop", "${prop}" );

		try {
			eval( "prop", this.root );
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = EvalException.class )
	public void loop2()
	{
		this.root.setValue( "prop", "1-${value1}-${value2}-2" );
		this.root.setValue( "value1", "${value}-1" );
		this.root.setValue( "value2", "${value}-2" );
		this.root.setValue( "value", "${prop}" );

		try {
			eval( "prop", this.root );
		}
		catch( final Exception e ) {
			System.err.println( e );

			throw e;
		}
	}

	@Test( expected = EvalException.class )
	public void loop3()
	{
		this.root.setValue( "prop", "${prop1:${prop2:${prop3:${prop}}}}" );

		try {
			eval( "prop", this.root );
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
			eval( "prop", this.root );
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

		final String x = eval( "prop", this.root );

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run2()
	{
		this.root.setValue( "prop.prop1", "value" );

		final String x = eval( "${prop.prop1}", this.root );

		assertThat( x, is( "value" ) );
	}

	@Test
	public void run3()
	{
		this.root.setValue( "prop1", "value1-${prop2}" );
		this.root.setValue( "prop2", "value2" );

		final String x = eval( "prefix-${prop1}-suffix", this.root );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void run4()
	{
		this.root.setValue( "prop1", "value1-${prop2:value2}" );

		final String x = eval( "prefix-${prop1}-suffix", this.root );

		assertThat( x, is( "prefix-value1-value2-suffix" ) );
	}

	@Test
	public void sys1()
	{
		final String x = eval( "java.version", this.root );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void sys2()
	{
		final String x = eval( "${java.version}", this.root );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void sys3()
	{
		final String x = eval( "version:${java.version}", this.root );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void sys4()
	{
		final String x = eval( "${version:${java.version}}", this.root );

		assertThat( x, is( System.getProperty( "java.version" ) ) );
	}

	@Test
	public void run1Def()
	{
		final String x = eval( "prop:default", this.root );

		assertThat( x, is( "default" ) );
	}

	@Test
	public void run2Def()
	{
		final String x = eval( "${prop:default}", this.root );

		assertThat( x, is( "default" ) );
	}
}
