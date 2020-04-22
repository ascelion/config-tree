
package ascelion.config.convert;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import ascelion.config.core.AbstractTest;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class StringConvertTest extends AbstractTest
{

	static private final Type STRING_COL = parameterizedClass( Collection.class, String.class );

	@Test
	public void fromPropertiesWithComma()
	{
		final Optional<String> v = this.root.getValue( "prop1.values1", String.class );
		final Optional<String[]> a = this.root.getValue( "prop1.values1", String[].class );
		final Optional<List<String>> c = this.root.getValue( "prop1.values1", STRING_COL );

		assertThat( v.isPresent(), is( false ) );
		assertThat( a.isPresent(), is( true ) );
		assertThat( c.isPresent(), is( true ) );

		assertThat( asList( a.get() ), equalTo( asList( "value111", "value112" ) ) );
		assertThat( c.get(), equalTo( asList( "value111", "value112" ) ) );
	}

	@Test
	public void fromProperties()
	{
		final Optional<String> v = this.root.getValue( "prop1.values2", String.class );
		final Optional<String[]> a = this.root.getValue( "prop1.values2", String[].class );
		final Optional<List<String>> c = this.root.getValue( "prop1.values2", STRING_COL );

		assertThat( v.isPresent(), is( true ) );
		assertThat( a.isPresent(), is( true ) );
		assertThat( c.isPresent(), is( true ) );

		assertThat( v.get(), equalTo( "value12" ) );
		assertThat( asList( a.get() ), equalTo( asList( "value12" ) ) );
		assertThat( c.get(), equalTo( asList( "value12" ) ) );
	}

	@Test
	public void fromYamlWithComma()
	{
		final Optional<String> v = this.root.getValue( "prop2.values1", String.class );
		final Optional<String[]> a = this.root.getValue( "prop2.values1", String[].class );
		final Optional<List<String>> c = this.root.getValue( "prop2.values1", STRING_COL );

		assertThat( v.isPresent(), is( true ) );
		assertThat( a.isPresent(), is( true ) );
		assertThat( c.isPresent(), is( true ) );

		assertThat( v.get(), equalTo( "value211,value212" ) );
		assertThat( asList( a.get() ), equalTo( asList( "value211,value212" ) ) );
		assertThat( c.get(), equalTo( asList( "value211,value212" ) ) );
	}

	@Test
	public void fromYamlWithArray()
	{
		final Optional<String> v = this.root.getValue( "prop2.values2", String.class );
		final Optional<String[]> a = this.root.getValue( "prop2.values2", String[].class );
		final Optional<List<String>> c = this.root.getValue( "prop2.values2", STRING_COL );

		assertThat( v.isPresent(), is( false ) );
		assertThat( a.isPresent(), is( true ) );
		assertThat( c.isPresent(), is( true ) );

		assertThat( asList( a.get() ), equalTo( asList( "value221", "value222" ) ) );
		assertThat( c.get(), equalTo( asList( "value221", "value222" ) ) );
	}
}
