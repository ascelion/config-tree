
package ascelion.config.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

import ascelion.config.api.ConfigParseException;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ExprParseTest
{

	@Parameterized.Parameters
	static public Object data()
	{
		final File base = new File( "src/test/resources" );

		return Stream.of( base.listFiles() )
			.filter( f -> f.getName().startsWith( "expression-" ) )
			.sorted( ( f1, f2 ) -> f1.getName().compareTo( f2.getName() ) )
			.map( f -> new Object[] { f } )
			.toArray();
	}

	@org.junit.Rule
	public ExpectedException exRule = ExpectedException.none();

	private final String content;
	private final int errors;

	public ExprParseTest( File file ) throws IOException
	{
		try( InputStream is = new FileInputStream( file ) ) {
			final List<String> lines = IOUtils.readLines( is, Charset.forName( "UTF-8" ) );

			assertThat( lines.size(), greaterThan( 0 ) );

			this.content = lines.get( 0 );
			this.errors = lines.size() > 1 ? Integer.valueOf( lines.get( 1 ) ) : 0;
		}
	}

	@Before
	public void setUp()
	{
		if( this.errors > 0 ) {
			this.exRule.expect( ConfigParseException.class );
		}
	}

	@Test
	public void parse()
	{
		System.out.println( "PARSE ----------------------" );
		System.out.printf( "'%s'\n", this.content );

		try {
			System.out.println( EvalTool.parse( this.content ) );
		}
		catch( final ConfigParseException e ) {
			e.getErrors().forEach( System.err::println );

			assertThat( e.getErrors(), hasSize( this.errors ) );

			throw e;
		}
	}

	@Test
	public void parseCnf()
	{
		System.out.println( "PARSE-CNF ----------------------" );
		System.out.printf( "'%s'\n", this.content );

		try {
			final ConfigNodeImpl node = new ConfigNodeImpl();

			node.set( this.content );
		}
		catch( final ConfigParseException e ) {
			e.getErrors().forEach( System.err::println );

			assertThat( e.getErrors(), hasSize( this.errors ) );

			throw e;
		}
	}
}
