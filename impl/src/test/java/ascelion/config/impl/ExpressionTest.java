
package ascelion.config.impl;

import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ExpressionTest
{

	static String mockEval( String x )
	{
		if( isBlank( x ) ) {
			return null;
		}

		return x.startsWith( "null-" ) || x.endsWith( "-null" ) ? null : "<" + x + ">";
	}

	@Test
	public void run01()
	{
		final Expression exp = new Expression( "{a-${b-${c}-d}-e}" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "{a-<b-<c>-d>-e}" ) );
	}

	@Test
	public void run02()
	{
		final Expression exp = new Expression( "x-${a-null:b}-y" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "x-b-y" ) );
	}

	@Test
	public void run03()
	{
		final Expression exp = new Expression( "x-${a-null:${b-null:c}}-y" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "x-c-y" ) );
	}

	@Test
	public void run04()
	{
		final Expression exp = new Expression( "{a-${b-${c-null:x-${y}-z}-d}-e}" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "{a-<b-x-<y>-z-d>-e}" ) );
	}

	@Test
	public void run05()
	{
		final Expression exp = new Expression( "$a:b" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "$a:b" ) );
	}

	@Test
	public void run06()
	{
		final Expression exp = new Expression( "${a\\:b}" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "<a:b>" ) );
	}

	@Test
	public void run07()
	{
		final Expression exp = new Expression( "${a-null:b:c}" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "b:c" ) );
	}

	@Test
	public void run08()
	{
		final Expression exp = new Expression( "${null-a\\:b:c}" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "c" ) );
	}

	@Test
	public void run09()
	{
		final Expression exp = new Expression( "\\$\\{x\\}" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "${x}" ) );
	}

	@Test
	public void run10()
	{
		final Expression exp = new Expression( "${a-null:\\$\\{x\\}}" );
		final String val = exp.eval( ExpressionTest::mockEval );

		assertThat( val, is( "<x>" ) );
	}

	@Test( expected = ConfigLoopException.class )
	public void runLoop()
	{
		final int[] count = { 0 };
		final UnaryOperator<String> fun = x -> {
			return format( "${X%02d}", ++count[0] % 5 );
		};
		final Expression exp = new Expression( "${X00}" );

		exp.eval( fun );
	}
}
