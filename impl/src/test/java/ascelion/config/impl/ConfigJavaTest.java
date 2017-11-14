
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ascelion.config.api.ConfigSource;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@ConfigSource( "file.properties" )
@ConfigSource( "file.conf" )
@ConfigSource( "file.ini" )
@ConfigSource( "file.yml" )
@RunWith( Parameterized.class )
public class ConfigJavaTest
{

	static <A> A[] A( A... a )
	{
		return a;
	}

	@Parameterized.Parameters( name = "{0} - {1} - {2}" )
	static public Object data()
	{
		final Map<String, String> strMap = new HashMap<>();

		strMap.put( "file.map1.values.VAL0", "0" );
		strMap.put( "file.map1.values.VAL1", "1" );
		strMap.put( "file.map1.values.VAL2", "2, 3, 4" );
		strMap.put( "file.map1.values.VAL3", "5,6,7" );

		final Map<String, int[]> intMap = new HashMap<>();

		intMap.put( "file.map1.values.VAL0", new int[] { 0 } );
		intMap.put( "file.map1.values.VAL1", new int[] { 1 } );
		intMap.put( "file.map1.values.VAL2", new int[] { 2, 3, 4 } );
		intMap.put( "file.map1.values.VAL3", new int[] { 5, 6, 7 } );

		return new Object[] {
			A( "file.prop1", String.class, "314" ),
			A( "file.prop1", Integer.class, 314 ),
			A( "file.prop1", int.class, 314 ),
			A( "file.prop1", Long.class, 314L ),
			A( "file.prop1", long.class, 314L ),
			A( "file.prop2", String.class, "value2" ),
			A( "file.prop3", String.class, "value1, value2, value3" ),
			A( "file.prop3", String[].class, A( "value1", "value2", "value3" ) ),

			A( "file.prop6", String.class, "1, 2, 3" ),
			A( "file.prop6", String[].class, A( "1", "2", "3" ) ),
			A( "file.prop6", Integer[].class, A( 1, 2, 3 ) ),
			A( "file.prop6", int[].class, new int[] { 1, 2, 3 } ),

			A( "file.prop6", parameterizedClass( Set.class, String.class ), new HashSet<>( asList( "1", "2", "3" ) ) ),
			A( "file.prop6", parameterizedClass( Set.class, Integer.class ), new HashSet<>( asList( 1, 2, 3 ) ) ),

			A( "file.prop6", parameterizedClass( List.class, String.class ), asList( "1", "2", "3" ) ),
			A( "file.prop6", parameterizedClass( List.class, Integer.class ), asList( 1, 2, 3 ) ),

			A( "file.map1.values", parameterizedClass( Map.class, String.class, String.class ), strMap ),
			A( "file.map1.values", parameterizedClass( Map.class, String.class, int[].class ), intMap ),
		};
	}

	private final String prop;
	private final Type type;
	private final Object expected;
	private final ConfigJava cj = new ConfigJava();

	public ConfigJavaTest( String prop, Type type, Object expected )
	{
		this.prop = prop;
		this.type = type;
		this.expected = expected;

		this.cj.add( s -> s.value().startsWith( "file" ) );
	}

	@Test
	public void run()
	{
		final Object o = this.cj.getValue( this.type, this.prop );

		assertThat( o, is( this.expected ) );
	}

}
