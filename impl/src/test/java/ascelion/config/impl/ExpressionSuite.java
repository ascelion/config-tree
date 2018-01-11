
package ascelion.config.impl;

import java.io.IOException;

import ascelion.config.api.ConfigParseException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ExpressionSuite
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return EvalData.suiteData();
	}

	@org.junit.Rule
	public ExpectedException exRule = ExpectedException.none();

	private final EvalData data;

	public ExpressionSuite( String name, EvalData data ) throws IOException
	{
		this.data = data;
	}

	@Before
	public void setUp()
	{
		if( this.data.errors > 0 ) {
			this.exRule.expect( ConfigParseException.class );
		}
	}

	@Test
	public void eval()
	{
		System.out.println( "EVAL ----------------------" );
		System.out.printf( "'%s'\n", this.data.expression );

		try {
			final Expression exp = new Expression( this.data.expression );
			final String val = exp.eval( ExpressionTest::mockEval );

			System.out.println( val );

			if( this.data.expected != null ) {
				assertThat( val, is( this.data.expected ) );
			}
		}
		catch( final ConfigParseException e ) {
			System.err.println( e );

			e.getErrors().forEach( System.err::println );

			assertThat( e.getErrors(), hasSize( this.data.errors ) );

			throw e;
		}
	}

}
