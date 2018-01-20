
package ascelion.config.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.text.StrLookup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.yaml.snakeyaml.Yaml;

@RunWith( Parameterized.class )
public class ExpressionSuite
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data() throws IOException
	{
		final List<Object[]> data = new ArrayList<>();
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();

		final Set<String> names = new LinkedHashSet<>();

		try( InputStream is = cld.getResourceAsStream( ExpressionSuite.class.getSimpleName() + ".yml" ) ) {
			final Yaml yml = new Yaml();
			final List<Map<String, Object>> all = (List<Map<String, Object>>) yml.load( is );

			for( final Map<String, Object> td : all ) {
				final String tn = ( (String) td.get( "expression" ) )
					.replace( "$", "@" )
					.replace( "\\", "^" );

				if( names.add( tn ) ) {
					data.add( new Object[] { format( "%02d-%s", names.size(), tn ), td } );
				}
				else {
					throw new RuntimeException( format( "%d: duplicated name: %s", names.size(), tn ) );
				}
			}
		}

		return data;
	}

	@Rule
	public ExpectedException xx = ExpectedException.none();

	private final String expression;
	private final String expected;
	private final String expectedCT;
	private final Map<String, String> properties;
	private final Class<? extends Exception> exception;
	private final Class<? extends Exception> exceptionCT;

	public ExpressionSuite( String unused, Map<String, Object> td ) throws ClassNotFoundException
	{
		this.expression = (String) td.get( "expression" );
		this.properties = (Map<String, String>) td.get( "properties" );
		this.expected = (String) td.get( "expected" );
		this.expectedCT = (String) td.get( "expectedCT" );
		this.exception = getException( td, "exception" );
		this.exceptionCT = getException( td, "exceptionCT" );
	}

	Class<? extends Exception> getException( Map<String, Object> td, String name ) throws ClassNotFoundException
	{
		final String exception = (String) td.get( name );

		if( exception != null ) {
			return (Class<? extends Exception>) Class.forName( exception );
		}
		else {
			return null;
		}
	}

	@Test
	public void commonsText()
	{
		if( this.exceptionCT != null ) {
			this.xx.expect( this.exceptionCT );
		}
		final StrSub sub = new StrSub();

		sub.setEscapeChar( '\\' );
		sub.setPreserveEscapes( true );
		sub.setEnableSubstitutionInVariables( true );
		sub.setVariableResolver( StrLookup.mapLookup( this.properties ) );

		final String text = unescapeJava( sub.replace( this.expression ) );

		if( this.expectedCT != null ) {
			assertThat( text, is( this.expectedCT ) );
		}
		else {
			assertThat( text, is( this.expected ) );
		}
	}

	@Test
	public void expression()
	{
		if( this.exception != null ) {
			this.xx.expect( this.exception );
		}

		final Expression exp = new Expression( this::lookup, this.expression );
		final String text = exp.getValue();

		assertThat( text, is( this.expected ) );
	}

	private String lookup( String key )
	{
		return this.properties != null ? this.properties.get( key ) : null;
	}

}
