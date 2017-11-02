
package ascelion.cdi.conf;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

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
			new Object[] { "root", 0 },
			new Object[] { "${root}", 0 },
			new Object[] { "item0.item1.item2", 0 },
			new Object[] { "${item0.item1.item2}", 0 },
			new Object[] { "${item0.${root.value}.item2}", 0 },
			new Object[] { "item0.${root.value}.item2", 0 },
			new Object[] { "item0.item1.item2}", 1 },
			new Object[] { "${${root}}", 0 },
			new Object[] { "${root.prop1:default}", 0 },
			new Object[] { "${root:${value}}", 0 },
			new Object[] { "${${root:default}}", 0 },
			new Object[] { "${${root}:default}", 0 },
			new Object[] { "${${root}:${value}}", 0 },
			new Object[] { "${${root:${value1}}}", 0 },
			new Object[] { "${${root:${value1}}:value2}", 0 },
			new Object[] { "$root", 1 },
			new Object[] { "${root", 1 },
			new Object[] { "${${root", 1 },
			new Object[] { "$root}", 2 },
			new Object[] { "root}", 1 },
			new Object[] { "{root}", 2 },
			new Object[] { "ro~ot", 1 },
			new Object[] { "${value}-1", 0 },
		};
	}

	private final String value;

	private final int errors;

	public ExpressionParserTest( String value, int errors )
	{
		this.value = value;
		this.errors = errors;
	}

	@Test
	public void run() throws IOException
	{
		final ExpressionParser px = ExpressionParser.createFor( this.value, System.err::println );

		px.setTrace( true );
		px.root();

		assertThat( px.getErrors(), hasSize( this.errors ) );
	}
}
