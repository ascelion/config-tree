
package ascelion.cdi.conf;

import ascelion.cdi.conf.ExpressionItem;

import static ascelion.cdi.conf.ExpressionItem.nextItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ExpressionItemTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			new Object[] { "root", null, null, null },
			new Object[] { "${root}", 2, 6, null },
			new Object[] { "${${root}}", 2, 9, null },
			new Object[] { "${root:default}", 2, 6, null },
			new Object[] { "${root:${value}}", 2, 6, null },
			new Object[] { "${${root:default}}", 2, 6, null },
			new Object[] { "${${root}:default}", 2, 6, null },
			new Object[] { "${${root}:${value}}", 2, 6, null },
			new Object[] { "${${root:${value1}}}", 2, 19, null },
			new Object[] { "${${root:${value1}}:value2}", 2, 26, null },
			new Object[] { "$root", null, null, IllegalArgumentException.class },
			new Object[] { "${root", null, null, IllegalArgumentException.class },
			new Object[] { "${${root", null, null, IllegalArgumentException.class },
			new Object[] { "$root}", null, null, IllegalArgumentException.class },
			new Object[] { "{root}", null, null, IllegalArgumentException.class },
		};
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Parameterized.Parameter( 0 )
	public String value;

	@Parameterized.Parameter( 1 )
	public Integer start;

	@Parameterized.Parameter( 2 )
	public Integer end;

	@Parameterized.Parameter( 3 )
	public Class<? extends Exception> exception;

	private ExpressionItem expected;

	@Before
	public void setUp()
	{
		if( this.exception != null ) {
			this.thrown.expect( this.exception );
		}
		if( this.start != null && this.end != null ) {
			this.expected = new ExpressionItem( this.value, this.start - 2, this.end + 1 );
		}
	}

	@Test
	public void run()
	{
		final ExpressionItem i = nextItem( this.value, 0 );

		assertThat( i, is( this.expected ) );
	}

}
