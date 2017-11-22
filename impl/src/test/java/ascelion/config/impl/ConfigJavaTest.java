
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ascelion.config.api.ConfigSource;

import static ascelion.config.impl.Utils.asArray;
import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
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

	@Parameterized.Parameters( name = "{0} - {1} - {2}" )
	static public Object data()
	{
		return new Object[] {
			asArray( "file.prop1", String.class, "314" ),
			asArray( "file.prop1", Integer.class, 314 ),
			asArray( "file.prop1", int.class, 314 ),
			asArray( "file.prop1", Long.class, 314L ),
			asArray( "file.prop1", long.class, 314L ),
			asArray( "file.prop2", String.class, "value2" ),
			asArray( "file.prop3", String.class, "value1, value2, value3" ),
			asArray( "file.prop3", String[].class, asArray( "value1", "value2", "value3" ) ),

			asArray( "file.prop6", String.class, "1, 2, 3" ),
			asArray( "file.prop6", String[].class, asArray( "1", "2", "3" ) ),
			asArray( "file.prop6", Integer[].class, asArray( 1, 2, 3 ) ),
			asArray( "file.prop6", int[].class, new int[] { 1, 2, 3 } ),

			asArray( "file.prop6", parameterizedClass( Set.class, String.class ), new HashSet<>( asList( "1", "2", "3" ) ) ),
			asArray( "file.prop6", parameterizedClass( Set.class, Integer.class ), new HashSet<>( asList( 1, 2, 3 ) ) ),

			asArray( "file.prop6", parameterizedClass( List.class, String.class ), asList( "1", "2", "3" ) ),
			asArray( "file.prop6", parameterizedClass( List.class, Integer.class ), asList( 1, 2, 3 ) ),

			asArray( "file.version1", String.class, System.getProperty( "java.version" ) ),
			asArray( "file.version2", String.class, System.getProperty( "java.version" ) ),
		};
	}

	static private final ConfigJava CJ = new ConfigJava();
	private final String prop;
	private final Type type;
	private final Object expected;

	public ConfigJavaTest( String prop, Type type, Object expected )
	{
		this.prop = prop;
		this.type = type;
		this.expected = expected;
	}

	@BeforeClass
	static public void setUpClass()
	{
		CJ.add( s -> s.value().startsWith( "file" ) || "SYS".equals( s.type() ) );
	}

	@Test
	public void run()
	{
		final Object o = CJ.getConverter().getValue( this.type, CJ.root().getNode( this.prop ), 0 );

		assertThat( o, is( this.expected ) );
	}

}
