
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ascelion.config.utils.Utils;
import ascelion.tests.WhiteBox;

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
			Utils.asArray( int[].class, Utils.asArray( lambda() ) ),
			Utils.asArray( arrayOf( long.class ), Utils.asArray( lambda() ) ),
			Utils.asArray( arrayOf( double.class ), Utils.asArray( lambda() ) ),

			Utils.asArray( arrayOf( String.class ), Utils.asArray( ArrayConverter.class, lambda() ) ),
			Utils.asArray( arrayOf( Integer.class ), Utils.asArray( ArrayConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( arrayOf( Long.class ), Utils.asArray( ArrayConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( arrayOf( Double.class ), Utils.asArray( ArrayConverter.class, NullableConverter.class, lambda() ) ),

			Utils.asArray( parameterizedClass( List.class, String.class ), Utils.asArray( ListConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( List.class, Integer.class ), Utils.asArray( ListConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( List.class, Long.class ), Utils.asArray( ListConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( List.class, URL.class ), Utils.asArray( ListConverter.class, NullableConverter.class, lambda() ) ),

			Utils.asArray( parameterizedClass( Set.class, String.class ), Utils.asArray( SetConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( Set.class, Integer.class ), Utils.asArray( SetConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( Set.class, Long.class ), Utils.asArray( SetConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( Set.class, URL.class ), Utils.asArray( SetConverter.class, NullableConverter.class, lambda() ) ),

			Utils.asArray( parameterizedClass( Map.class, String.class, String.class ), Utils.asArray( MapConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( Map.class, String.class, Integer.class ), Utils.asArray( MapConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( Map.class, String.class, Long.class ), Utils.asArray( MapConverter.class, NullableConverter.class, lambda() ) ),
			Utils.asArray( parameterizedClass( Map.class, String.class, URL.class ), Utils.asArray( MapConverter.class, NullableConverter.class, lambda() ) ),

			Utils.asArray( arrayOf( DataSourceDefinition.class ), Utils.asArray( ArrayConverter.class, InterfaceConverter.class ) ),
			Utils.asArray( parameterizedClass( List.class, DataSourceDefinition.class ), Utils.asArray( ListConverter.class, InterfaceConverter.class ) ),
			Utils.asArray( parameterizedClass( Set.class, DataSourceDefinition.class ), Utils.asArray( SetConverter.class, InterfaceConverter.class ) ),
			Utils.asArray( parameterizedClass( Map.class, String.class, DataSourceDefinition.class ), Utils.asArray( MapConverter.class, InterfaceConverter.class ) ),

			Utils.asArray( parameterizedClass( List.class, parameterizedClass( Set.class, arrayOf( parameterizedClass( Map.class, String.class, int[].class ) ) ) ),
				Utils.asArray( ListConverter.class, SetConverter.class, ArrayConverter.class, MapConverter.class, lambda() ) ),

			Utils.asArray( parameterizedClass( List.class, parameterizedClass( Set.class, arrayOf( parameterizedClass( Map.class, String.class, DataSourceDefinition.class ) ) ) ),
				Utils.asArray( ListConverter.class, SetConverter.class, ArrayConverter.class, MapConverter.class, InterfaceConverter.class ) ),
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
