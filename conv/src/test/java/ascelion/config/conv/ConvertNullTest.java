
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ascelion.config.utils.Utils;

import static ascelion.config.conv.Utils.isArray;
import static ascelion.config.conv.Utils.isPrimitive;
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

@RunWith( Parameterized.class )
public class ConvertNullTest
{

	static private final Converters CVS = new Converters();

	@Parameterized.Parameters( name = "{0}" )
	static public Object[] data()
	{
		final List<Object[]> data = new ArrayList<>();
		final Map<Type, ?> cached = CVS.getConverters();

		for( final Type t : cached.keySet() ) {
			if( isPrimitive( t ) ) {
				data.add( Utils.asArray( t, notNullValue() ) );
			}
			else if( isArray( t ) ) {
				data.add( Utils.asArray( t, notNullValue() ) );
			}
			else {
				data.add( Utils.asArray( t, nullValue() ) );
			}
		}

		data.add( Utils.asArray( parameterizedClass( Map.class, String.class, String.class ), notNullValue() ) );
		data.add( Utils.asArray( parameterizedClass( List.class, String.class ), notNullValue() ) );
		data.add( Utils.asArray( parameterizedClass( Set.class, String.class ), notNullValue() ) );
		data.add( Utils.asArray( arrayOf( String.class ), notNullValue() ) );

		data.add( Utils.asArray( URI.class, nullValue() ) );

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
		assertThat( (Object) CVS.getConverter( this.t ).create( null ), is( this.m ) );
	}

	@Test
	public void node()
	{
		assertThat( (Object) CVS.getConverter( this.t ).create( null, 0 ), is( this.m ) );
	}
}
