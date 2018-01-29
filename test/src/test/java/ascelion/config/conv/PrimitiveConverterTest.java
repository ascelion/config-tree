
package ascelion.config.conv;

import java.util.ArrayList;
import java.util.List;

import ascelion.config.impl.ConfigNodeImpl;

import static ascelion.config.utils.Utils.asArray;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
public class PrimitiveConverterTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		final List<Object[]> data = new ArrayList<>();

		data.add( asArray( boolean.class, false ) );
		data.add( asArray( byte.class, (byte) 0 ) );
		data.add( asArray( short.class, (short) 0 ) );
		data.add( asArray( int.class, 0 ) );
		data.add( asArray( long.class, 0L ) );
		data.add( asArray( float.class, 0F ) );
		data.add( asArray( double.class, 0D ) );

		return data;
	}

	private final Class<?> type;
	private final Object expected;

	public PrimitiveConverterTest( Class<?> type, Object expected )
	{
		this.type = type;
		this.expected = expected;
	}

	@Test
	public void nullNode()
	{
		final Converters cvs = new Converters();
		final Object value = cvs.getConverter( this.type ).create( null, 0 );

		assertThat( value, is( this.expected ) );
	}

	@Test
	public void nullNodeValue()
	{
		final Converters cvs = new Converters();
		final Object value = cvs.getConverter( this.type ).create( new ConfigNodeImpl(), 0 );

		assertThat( value, is( this.expected ) );
	}

	@Test
	public void nullValue()
	{
		final Converters cvs = new Converters();
		final Object value = cvs.getConverter( this.type ).create( null );

		assertThat( value, is( this.expected ) );
	}

	@Test
	public void emptyValue()
	{
		final Converters cvs = new Converters();
		final Object value = cvs.getConverter( this.type ).create( "" );

		assertThat( value, is( this.expected ) );
	}

}
