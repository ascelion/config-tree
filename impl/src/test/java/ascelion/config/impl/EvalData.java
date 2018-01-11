
package ascelion.config.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

final class EvalData
{

	static private String testName( File file )
	{
		return file.getName().replaceAll( "^expression-", "" ).replaceAll( "\\.txt$", "" );
	}

	static public Object suiteData()
	{
		final File base = new File( "src/test/resources" );

		return Stream.of( base.listFiles() )
			.filter( f -> f.getName().startsWith( "expression-" ) )
			.sorted( ( f1, f2 ) -> f1.getName().compareTo( f2.getName() ) )
			.map( f -> new Object[] { testName( f ), new EvalData( f ) } )
			.toArray();
	}

	final String expression;
	final String expected;
	final int errors;

	private EvalData( File file )
	{
		final List<String> lines;

		try {
			lines = FileUtils.readLines( file, "UTF-8" );
		}
		catch( final IOException e ) {
			throw new RuntimeException( file.getAbsolutePath(), e );
		}

		this.expression = lines.get( 0 );

		if( lines.size() > 1 ) {
			final String next = lines.get( 1 );

			if( next.startsWith( "=" ) ) {
				this.expected = next.substring( 1 );
				this.errors = 0;
			}
			else {
				this.expected = null;
				this.errors = Integer.valueOf( next );
			}
		}
		else {
			this.expected = this.expression;
			this.errors = 0;
		}
	}
}
