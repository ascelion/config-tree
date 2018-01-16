
package ascelion.config.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BufferTest
{

	@Rule
	public ExpectedException exex = ExpectedException.none();

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

		this.exex.expect( ArrayIndexOutOfBoundsException.class );

		buf.delete( 0 );
		assertThat( buf.toString(), is( "" ) );
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

	@Test
	public void startsWith()
	{
		final Buffer buf = new Buffer( "abc" );

		assertTrue( buf.startsWith( "" ) );
		assertTrue( buf.startsWith( "a" ) );
		assertTrue( buf.startsWith( "ab" ) );
		assertTrue( buf.startsWith( "abc" ) );
		assertFalse( buf.startsWith( "abcd" ) );
	}

	@Test
	public void find()
	{
		final Buffer buf = new Buffer( "abc" );

		assertThat( 0, is( buf.find( "" ) ) );
		assertThat( 0, is( buf.find( "a" ) ) );
		assertThat( 0, is( buf.find( "ab" ) ) );
		assertThat( 0, is( buf.find( "abc" ) ) );
		assertThat( -1, is( buf.find( "abcd" ) ) );

		assertThat( 1, is( buf.find( "b" ) ) );
		assertThat( 1, is( buf.find( "bc" ) ) );
		assertThat( -1, is( buf.find( "bcd" ) ) );
	}

}
