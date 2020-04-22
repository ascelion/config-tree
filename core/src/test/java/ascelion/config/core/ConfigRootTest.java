
package ascelion.config.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import ascelion.config.read.PropertiesInputReader;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ConfigRootTest
{

	private final ConfigRootImpl root = new ConfigRootImpl();

	@Test
	public void build()
	{
		final PropertiesInputReader reader = new PropertiesInputReader();

		this.root.addConfigInputs( reader.read( getClass().getSimpleName() ) );

		checkValue( this.root, "1", "1" );
		checkValue( this.root, "1.1", "11" );
		checkValue( this.root, "1.2", "12" );

		checkValue( this.root, "2", "2" );
		checkValue( this.root, "2.1", "21" );
		checkValue( this.root, "2.2", "22" );
	}

	@Test
	public void priority()
	{
		final PropertiesInputReader reader = new PropertiesInputReader();

		this.root.addConfigInputs( reader.read( getClass().getSimpleName() ) );
		this.root.addConfigInputs( reader.read( getClass().getSimpleName() + "_X" ) );

		checkValue( this.root, "1", "X1" );
		checkValue( this.root, "1.1", "X11" );
		checkValue( this.root, "1.2", "X12" );

		checkValue( this.root, "2", "X2" );
		checkValue( this.root, "2.1", "X21" );
		checkValue( this.root, "2.2", "X22" );
	}

	private void checkValue( ConfigRootImpl root, String path, String expected )
	{
		final Optional<String> value = root.getValue( path, String.class );

		assertThat( path, value.isPresent(), equalTo( expected != null ) );

		if( expected != null ) {
			assertThat( value.get(), equalTo( expected ) );
		}
	}
}
