
package ascelion.config.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

public class BufferTest implements TestExecutionExceptionHandler
{

	private Class<?> expected;

	@Test
	public void delete()
	{
		final Buffer buf = new Buffer( "abcdef" );

		assertThat( buf.delete( 4, 2 ), is( 2 ) );
		assertThat( buf.toString(), is( "abcd" ) );
		assertThat( buf.delete( 3, 1 ), is( 1 ) );
		assertThat( buf.toString(), is( "abc" ) );

		assertThat( buf.delete( 1 ), is( 1 ) );
		assertThat( buf.toString(), is( "ac" ) );

		assertThat( buf.delete( 0 ), is( 1 ) );
		assertThat( buf.toString(), is( "c" ) );

		buf.delete( 0 );
		assertThat( buf.toString(), is( "" ) );

		this.expected = ArrayIndexOutOfBoundsException.class;

		assertThrows( ArrayIndexOutOfBoundsException.class, () -> {
			buf.delete( 0 );
		} );
	}

	@Test
	public void replace()
	{
		final Buffer buf = new Buffer( "abc" );

		assertThat( buf.replace( 1, 1, "B" ), is( 0 ) );
		assertThat( buf.toString(), is( "aBc" ) );

		assertThat( buf.replace( 1, 0, "XYZ" ), is( 3 ) );
		assertThat( buf.toString(), is( "aXYZBc" ) );

		assertThat( buf.replace( 1, 5, "AB" ), is( -3 ) );
		assertThat( buf.toString(), is( "aAB" ) );
	}

	@Override
	public void handleTestExecutionException( ExtensionContext context, Throwable throwable ) throws Throwable
	{
		if( this.expected != null && this.expected.isInstance( throwable ) ) {
			return;
		}

		throw throwable;
	}

}
