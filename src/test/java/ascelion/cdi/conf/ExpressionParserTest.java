
package ascelion.cdi.conf;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ExpressionParserTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			new Object[] { "root", 0, },
			new Object[] { "item0.item1.item2", 0, },
			new Object[] { "${root}", 0, },
			new Object[] { "${${root}}", 0, },
			new Object[] { "${root:default}", 0, },
			new Object[] { "${root:${value}}", 0, },
			new Object[] { "${${root:default}}", 0, },
			new Object[] { "${${root}:default}", 0, },
			new Object[] { "${${root}:${value}}", 0, },
			new Object[] { "${${root:${value1}}}", 0, },
			new Object[] { "${${root:${value1}}:value2}", 0, },
			new Object[] { "$root", 1, },
			new Object[] { "${root", 1, },
			new Object[] { "${${root", 1, },
			new Object[] { "$root}", 1, },
			new Object[] { "{root}", 1, },
			new Object[] { "ro~ot", 1, },
		};
	}

	@Parameterized.Parameter( 0 )
	public String value;

	@Parameterized.Parameter( 1 )
	public int errors;

	@Test
	public void run()
	{
		final CodePointCharStream cs = CharStreams.fromString( this.value );
		final ExpressionLexer lx = new ExpressionLexer( cs );
		final CommonTokenStream ts = new CommonTokenStream( lx );
		final ExpressionParser px = new ExpressionParser( ts );

		px.addParseListener( new ExpressionParserListener() );

		px.expr();

		assertThat( px.getNumberOfSyntaxErrors(), is( this.errors ) );
	}

}
