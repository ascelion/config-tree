
package ascelion.config.eval;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BufferTest
{

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

		assertThrows( ArrayIndexOutOfBoundsException.class, () -> buf.delete( 0 ) );
	}

	@Test
	public void match()
	{
		final Buffer buf = new Buffer( "abc" );

		assertThat( buf.match( "a".toCharArray(), 0 ), equalTo( 0 ) );
		assertThat( buf.match( "a".toCharArray(), 1 ), equalTo( 2 ) );
		assertThat( buf.match( "a".toCharArray(), 2 ), equalTo( 3 ) );

		assertThat( buf.match( "b".toCharArray(), 0 ), equalTo( 1 ) );
		assertThat( buf.match( "b".toCharArray(), 1 ), equalTo( 1 ) );
		assertThat( buf.match( "b".toCharArray(), 2 ), equalTo( 3 ) );

		assertThat( buf.match( "c".toCharArray(), 0 ), equalTo( 1 ) );
		assertThat( buf.match( "c".toCharArray(), 1 ), equalTo( 2 ) );
		assertThat( buf.match( "c".toCharArray(), 2 ), equalTo( 2 ) );
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

}
