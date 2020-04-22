
package ascelion.config.convert;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import ascelion.config.core.AbstractTest;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class IntPrimitiveConvertTest extends AbstractTest
{

	public IntPrimitiveConvertTest()
	{
		super( IntConvertTest.class.getSimpleName() );
	}

	@Test
	public void fromPropertiesWithComma()
	{
		final Optional<int[]> a = this.root.getValue( "prop1.values1", int[].class );

		assertThat( a.isPresent(), is( true ) );
		assertThat( a.get(), equalTo( new int[] { 111, 112 } ) );
	}

	@Test
	public void fromProperties()
	{
		final Optional<int[]> a = this.root.getValue( "prop1.values2", int[].class );

		assertThat( a.isPresent(), is( true ) );
		assertThat( a.get(), equalTo( new int[] { 12 } ) );
	}

	@Test
	public void fromYamlWithArray()
	{
		final Optional<int[]> a = this.root.getValue( "prop2.values2", int[].class );

		assertThat( a.isPresent(), is( true ) );

		assertThat( a.get(), equalTo( new int[] { 221, 222 } ) );
	}
}
