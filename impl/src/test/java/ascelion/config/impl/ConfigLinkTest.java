
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static ascelion.config.impl.Utils.asArray;
import static ascelion.config.impl.Utils.asSet;
import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@ConfigSource( "file.properties" )
@ConfigSource( "file.conf" )
@ConfigSource( "file.ini" )
@ConfigSource( "file.yml" )
@ConfigSource( type = ConfigLinkTest.SOURCE_TYPE )
@RunWith( Parameterized.class )
public class ConfigLinkTest
{

	@ConfigReader.Type( SOURCE_TYPE )
	static public class CustomReader implements ConfigReader
	{

		@Override
		public Map<String, ?> readConfiguration( ConfigSource source ) throws ConfigException
		{
			return singletonMap( "java.version", System.getProperty( "java.version" ) );
		}
	};

	static final String SOURCE_TYPE = "ConfigLinkTest";

	static private final String PREFIX = "file";
	static private final String PROP1 = PREFIX + ".map1.values";
	static private final String PROP2 = "${" + PREFIX + ".map2.values}";

	static <V> Object[] map( Type type, V... values )
	{
		final Map<String, V> m = new HashMap<>();

		for( final V v : values ) {
			m.put( format( "%s.VAL%d", PROP1.substring( PREFIX.length() + 1 ), m.size() ), v );
		}

		return asArray( parameterizedClass( Map.class, String.class, type ), m );
	}

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			map( String.class, "0", "1", "2, 3, 4", "5,6,7" ),

			map( int[].class, new int[] { 0 }, new int[] { 1 }, new int[] { 2, 3, 4 }, new int[] { 5, 6, 7 } ),
			map( long[].class, new long[] { 0 }, new long[] { 1 }, new long[] { 2, 3, 4 }, new long[] { 5, 6, 7 } ),
			map( double[].class, new double[] { 0 }, new double[] { 1 }, new double[] { 2, 3, 4 }, new double[] { 5, 6, 7 } ),

			map( String[].class, asArray( "0" ), asArray( "1" ), asArray( "2", "3", "4" ), asArray( "5", "6", "7" ) ),
			map( Integer[].class, new Integer[] { 0 }, new Integer[] { 1 }, new Integer[] { 2, 3, 4 }, new Integer[] { 5, 6, 7 } ),
			map( Long[].class, new Long[] { 0L }, new Long[] { 1L }, new Long[] { 2L, 3L, 4L }, new Long[] { 5L, 6L, 7L } ),
			map( Double[].class, new Double[] { 0D }, new Double[] { 1D }, new Double[] { 2D, 3D, 4D }, new Double[] { 5D, 6D, 7D } ),

			map( parameterizedClass( List.class, String.class ), asList( "0" ), asList( "1" ), asList( "2", "3", "4" ), asList( "5", "6", "7" ) ),
			map( parameterizedClass( List.class, Integer.class ), asList( 0 ), asList( 1 ), asList( 2, 3, 4 ), asList( 5, 6, 7 ) ),
			map( parameterizedClass( List.class, Long.class ), asList( 0L ), asList( 1L ), asList( 2L, 3L, 4L ), asList( 5L, 6L, 7L ) ),
			map( parameterizedClass( List.class, Double.class ), asList( 0D ), asList( 1D ), asList( 2D, 3D, 4D ), asList( 5D, 6D, 7D ) ),

			map( parameterizedClass( Set.class, String.class ), asSet( "0" ), asSet( "1" ), asSet( "2", "3", "4" ), asSet( "5", "6", "7" ) ),
			map( parameterizedClass( Set.class, Integer.class ), asSet( 0 ), asSet( 1 ), asSet( 2, 3, 4 ), asSet( 5, 6, 7 ) ),
			map( parameterizedClass( Set.class, Long.class ), asSet( 0L ), asSet( 1L ), asSet( 2L, 3L, 4L ), asSet( 5L, 6L, 7L ) ),
			map( parameterizedClass( Set.class, Double.class ), asSet( 0D ), asSet( 1D ), asSet( 2D, 3D, 4D ), asSet( 5D, 6D, 7D ) ),
		};
	}

	static private final ConfigJava CJ = new ConfigJava();
	static private final ConfigConverter<?> CV = CJ.getConverter();

	@BeforeClass
	static public void setUpClass()
	{
		CJ.add( s -> s.value().startsWith( "file" ) || SOURCE_TYPE.equals( s.type() ) );
	}

	private final Type type;
	private final Map<String, ?> expected;

	public ConfigLinkTest( Type type, Map<String, ?> expected )
	{
		this.type = type;
		this.expected = expected;
	}

	@Before
	public void setUp()
	{
		System.out.printf( "Type: %s\n", this.type.getTypeName() );
	}

	@After
	public void tearDown()
	{
		System.out.println();
	}

	@Test
	public void run1()
	{
		verify( CJ.root().getNode( PROP1 ) );
	}

	@Test
	public void run2()
	{
		verify( CJ.root().getNode( PROP2 ) );
	}

	public void verify( final ConfigNode n )
	{
		final Object o = CV.create( this.type, n, 1 );

		assertThat( o, is( instanceOf( Map.class ) ) );

		final Map<String, ?> m = (Map<String, ?>) o;

		assertThat( m.size(), is( this.expected.size() ) );

		this.expected.forEach( ( k, v ) -> {
			final Object t = m.get( k );

			assertThat( k, t, is( v ) );
		} );
	}

}
