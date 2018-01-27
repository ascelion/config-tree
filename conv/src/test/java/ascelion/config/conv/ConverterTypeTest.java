
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ascelion.tests.WhiteBox;

import static ascelion.config.conv.Utils.asArray;
import static io.leangen.geantyref.TypeFactory.arrayOf;
import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class ConverterTypeTest
{

	static <T> IsLambda<T> lambda()
	{
		return new IsLambda<>();
	}

	static class IsLambda<T> extends BaseMatcher<T>
	{

		@Override
		public boolean matches( Object item )
		{
			if( item == null ) {
				return false;
			}

			final Class<?> c;

			if( item instanceof Class ) {
				c = (Class<?>) item;
			}
			else {
				c = item.getClass();
			}

			return c.getSimpleName().contains( "$$Lambda$" );
		}

		@Override
		public void describeTo( Description description )
		{
			description.appendText( "lambda" );
		}
	}

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			asArray( int[].class, asArray( lambda() ) ),
			asArray( arrayOf( long.class ), asArray( lambda() ) ),
			asArray( arrayOf( double.class ), asArray( lambda() ) ),

			asArray( arrayOf( String.class ), asArray( ArrayConverter.class, lambda() ) ),
			asArray( arrayOf( Integer.class ), asArray( ArrayConverter.class, NullableConverter.class, lambda() ) ),
			asArray( arrayOf( Long.class ), asArray( ArrayConverter.class, NullableConverter.class, lambda() ) ),
			asArray( arrayOf( Double.class ), asArray( ArrayConverter.class, NullableConverter.class, lambda() ) ),

			asArray( parameterizedClass( List.class, String.class ), asArray( ListConverter.class, lambda() ) ),
			asArray( parameterizedClass( List.class, Integer.class ), asArray( ListConverter.class, NullableConverter.class, lambda() ) ),
			asArray( parameterizedClass( List.class, Long.class ), asArray( ListConverter.class, NullableConverter.class, lambda() ) ),
			asArray( parameterizedClass( List.class, URL.class ), asArray( ListConverter.class, NullableConverter.class, lambda() ) ),

			asArray( parameterizedClass( Set.class, String.class ), asArray( SetConverter.class, lambda() ) ),
			asArray( parameterizedClass( Set.class, Integer.class ), asArray( SetConverter.class, NullableConverter.class, lambda() ) ),
			asArray( parameterizedClass( Set.class, Long.class ), asArray( SetConverter.class, NullableConverter.class, lambda() ) ),
			asArray( parameterizedClass( Set.class, URL.class ), asArray( SetConverter.class, NullableConverter.class, lambda() ) ),

			asArray( parameterizedClass( Map.class, String.class, String.class ), asArray( MapConverter.class, lambda() ) ),
			asArray( parameterizedClass( Map.class, String.class, Integer.class ), asArray( MapConverter.class, NullableConverter.class, lambda() ) ),
			asArray( parameterizedClass( Map.class, String.class, Long.class ), asArray( MapConverter.class, NullableConverter.class, lambda() ) ),
			asArray( parameterizedClass( Map.class, String.class, URL.class ), asArray( MapConverter.class, NullableConverter.class, lambda() ) ),

			asArray( arrayOf( DataSourceDefinition.class ), asArray( ArrayConverter.class, InterfaceConverter.class, Converters.class ) ),
			asArray( parameterizedClass( List.class, DataSourceDefinition.class ), asArray( ListConverter.class, InterfaceConverter.class, Converters.class ) ),
			asArray( parameterizedClass( Set.class, DataSourceDefinition.class ), asArray( SetConverter.class, InterfaceConverter.class, Converters.class ) ),
			asArray( parameterizedClass( Map.class, String.class, DataSourceDefinition.class ), asArray( MapConverter.class, InterfaceConverter.class, Converters.class ) ),

			asArray( parameterizedClass( List.class, parameterizedClass( Set.class, arrayOf( parameterizedClass( Map.class, String.class, int[].class ) ) ) ),
				asArray( ListConverter.class, SetConverter.class, ArrayConverter.class, MapConverter.class, lambda() ) ),

			asArray( parameterizedClass( List.class, parameterizedClass( Set.class, arrayOf( parameterizedClass( Map.class, String.class, DataSourceDefinition.class ) ) ) ),
				asArray( ListConverter.class, SetConverter.class, ArrayConverter.class, MapConverter.class, InterfaceConverter.class, Converters.class ) ),
		};
	}

	private final Converters cvs = new Converters();
	private final Type type;
	private final Object[] expected;

	public ConverterTypeTest( Type type, Object[] classes )
	{
		this.type = type;
		this.expected = classes;
	}

	@Test
	public void run()
	{
		Object o = this.cvs.getConverter( this.type );

		for( final Object exp : this.expected ) {
			assertThat( o, is( notNullValue() ) );

			if( exp instanceof Class ) {
				assertThat( o, is( instanceOf( (Class) exp ) ) );
			}
			else {
				assertThat( o, is( (Matcher) exp ) );
			}

			try {
				o = WhiteBox.getFieldValue( o, "conv" );
			}
			catch( final RuntimeException e ) {
				o = null;
			}
		}
	}
}