
package ascelion.config.cdi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ascelion.config.api.ConfigValue;
import ascelion.config.core.AbstractTest;

import java.util.NoSuchElementException;
import java.util.Optional;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import org.junit.jupiter.api.Test;

public class ConfigRequiredTest extends AbstractTest
{

	static class Bean1
	{

		@ConfigValue
		String undefined;
	}

	@Test
	public void undefined()
	{
		@SuppressWarnings( "unchecked" )
		final SeContainerInitializer weld = SeContainerInitializer.newInstance()
			.addExtensions( ConfigExtension.class )
			.addBeanClasses( Bean1.class );

		try( SeContainer cont = weld.initialize() ) {
			assertThrows( NoSuchElementException.class, () -> {
				cont.select( Bean1.class ).get();
			} );
		}
	}

	static class Bean2
	{

		@ConfigValue
		Optional<String> undefined;
	}

	@Test
	public void optUndefined()
	{
		@SuppressWarnings( "unchecked" )
		final SeContainerInitializer weld = SeContainerInitializer.newInstance()
			.addExtensions( ConfigExtension.class )
			.addBeanClasses( Bean2.class );

		try( SeContainer cont = weld.initialize() ) {
			final Bean2 bean = cont.select( Bean2.class ).get();

			assertThat( bean.undefined, is( notNullValue() ) );
			assertThat( bean.undefined.isPresent(), is( false ) );
		}
	}

	static class Bean3
	{

		@ConfigValue( required = false )
		String undefined;
	}

	@Test
	public void notRequiredUndefined()
	{
		@SuppressWarnings( "unchecked" )
		final SeContainerInitializer weld = SeContainerInitializer.newInstance()
			.addExtensions( ConfigExtension.class )
			.addBeanClasses( Bean3.class );

		try( SeContainer cont = weld.initialize() ) {
			final Bean3 bean = cont.select( Bean3.class ).get();

			assertThat( bean.undefined, is( nullValue() ) );
		}
	}

	static class Bean4
	{

		@ConfigValue( "reference.to.value" )
		String undefined;
	}

	@Test
	public void referenceToUndefined()
	{
		@SuppressWarnings( "unchecked" )
		final SeContainerInitializer weld = SeContainerInitializer.newInstance()
			.addExtensions( ConfigExtension.class )
			.addBeanClasses( Bean4.class );

		System.setProperty( "defined", "${undefined}" );
		System.setProperty( "reference.to.value", "reference.to.${defined}" );

		try( SeContainer cont = weld.initialize() ) {
			assertThrows( NoSuchElementException.class, () -> {
				cont.select( Bean4.class ).get();
			} );
		}
	}
}
