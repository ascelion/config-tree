
package ascelion.config.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import static java.lang.String.format;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.commons.text.StrLookup;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.Yaml;

public class ExpressionSuite
{

	static public Stream<Arguments> arguments() throws IOException
	{
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();
		final Set<String> names = new LinkedHashSet<>();

		final Builder<Arguments> sb = Stream.<Arguments> builder();

		try( InputStream is = cld.getResourceAsStream( ExpressionSuite.class.getSimpleName() + ".yml" ) ) {
			final Yaml yml = new Yaml();
			final List<Map<String, Object>> all = (List<Map<String, Object>>) yml.load( is );

			for( final Map<String, Object> td : all ) {
				final String tn = ( (String) td.get( "expression" ) )
					.replace( "$", "@" )
					.replace( "\\", "^" );

				sb.add( Arguments.of( format( "%02d-%s", names.size(), tn ), td ) );
			}
		}

		return sb.build();
	}

	static Class<? extends Exception> getException( Map<String, Object> td, String name ) throws ClassNotFoundException
	{
		final String exception = (String) td.get( name );

		if( exception != null ) {
			return (Class<? extends Exception>) Class.forName( exception );
		}
		else {
			return null;
		}
	}

	private String expression;
	private String expected;
	private String expectedCT;
	private Map<String, String> properties;
	private Class<? extends Exception> exception;
	private Class<? extends Exception> exceptionCT;

	private void setUp( Map<String, Object> td ) throws ClassNotFoundException
	{
		this.expression = (String) td.get( "expression" );
		this.properties = (Map<String, String>) td.get( "properties" );
		this.expected = (String) td.get( "expected" );
		this.expectedCT = (String) td.get( "expectedCT" );
		this.exception = getException( td, "exception" );
		this.exceptionCT = getException( td, "exceptionCT" );
	}

	@ParameterizedTest
	@MethodSource( "arguments" )
	public void commonsText( String unused, Map<String, Object> td ) throws Throwable
	{
		try {
			setUp( td );

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
		catch( final Throwable t ) {
			handleTestExecutionException( t );
		}
	}

	@ParameterizedTest
	@MethodSource( "arguments" )
	public void expression( String unused, Map<String, Object> td ) throws Throwable
	{
		try {
			setUp( td );

			final Expression exp = new Expression( this::lookup, this.expression );
			final String text = exp.getValue();

			assertThat( text, is( this.expected ) );
		}
		catch( final Throwable t ) {
			handleTestExecutionException( t );
		}
	}

	private String lookup( String key )
	{
		return this.properties != null ? this.properties.get( key ) : null;
	}

	public void handleTestExecutionException( Throwable throwable ) throws Throwable
	{
		if( this.exception != null && this.exception.isInstance( throwable ) ) {
			return;
		}
		if( this.exceptionCT != null && this.exceptionCT.isInstance( throwable ) ) {
			return;
		}

		throw throwable;
	}

}
