
package ascelion.config.impl;

import java.io.IOException;

import ascelion.config.api.ConfigParseException;
import ascelion.config.impl.EvalTool.Expr;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ExprParseTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return EvalData.suiteData();
	}

	@org.junit.Rule
	public ExpectedException exRule = ExpectedException.none();

	private final EvalData data;

	public ExprParseTest( String name, EvalData data ) throws IOException
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
	public void parse()
	{
		System.out.println( "PARSE ----------------------" );
		System.out.printf( "'%s'\n", this.data.expression );

		try {
			final Expr exp = EvalTool.parse( this.data.expression );

			System.out.println( exp );
		}
		catch( final ConfigParseException e ) {
			e.getErrors().forEach( System.err::println );

			assertThat( e.getErrors(), hasSize( this.data.errors ) );

			throw e;
		}
	}

	@Test
	public void parseCnf()
	{
		System.out.println( "PARSE-CNF ----------------------" );
		System.out.printf( "'%s'\n", this.data.expression );

		try {
			final ConfigNodeImpl node = new ConfigNodeImpl();

			node.set( this.data.expression );
		}
		catch( final ConfigParseException e ) {
			e.getErrors().forEach( System.err::println );

			assertThat( e.getErrors(), hasSize( this.data.errors ) );

			throw e;
		}
	}
}
