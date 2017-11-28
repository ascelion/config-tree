
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ascelion.config.impl.Utils.asArray;
import static ascelion.config.impl.Utils.isArray;
import static ascelion.config.impl.Utils.isPrimitive;
import static io.leangen.geantyref.TypeFactory.arrayOf;
import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.internal.util.reflection.Whitebox;

@RunWith( Parameterized.class )
public class ConvertNullTest
{

	static private final Converters CVS = new Converters();

	@Parameterized.Parameters( name = "{0}" )
	static public Object[] data()
	{
		final List<Object[]> data = new ArrayList<>();
		final Map<Type, ?> cached = (Map<Type, ?>) Whitebox.getInternalState( CVS, "cached" );

		for( final Type t : cached.keySet() ) {
			if( isPrimitive( t ) ) {
				data.add( asArray( t, notNullValue() ) );
			}
			else if( isArray( t ) ) {
				data.add( asArray( t, notNullValue() ) );
			}
			else {
				data.add( asArray( t, nullValue() ) );
			}
		}

		data.add( asArray( parameterizedClass( Map.class, String.class, String.class ), notNullValue() ) );
		data.add( asArray( parameterizedClass( List.class, String.class ), notNullValue() ) );
		data.add( asArray( parameterizedClass( Set.class, String.class ), notNullValue() ) );
		data.add( asArray( arrayOf( String.class ), notNullValue() ) );

		data.add( asArray( URI.class, nullValue() ) );

		return data.toArray();
	}

	private final Type t;
	private final Matcher m;

	public ConvertNullTest( Type type, Matcher m )
	{
		this.t = type;
		this.m = m;
	}

	@Test
	public void string()
	{
		assertThat( CVS.create( this.t, null ), is( this.m ) );
	}

	@Test
	public void node()
	{
		assertThat( CVS.create( this.t, null, 0 ), is( this.m ) );
	}
}
