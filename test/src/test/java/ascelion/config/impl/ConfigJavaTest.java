
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConvertersRegistry;
import ascelion.config.utils.Utils;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.Before;
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
			Utils.asArray( "file.prop1", String.class, "314" ),
			Utils.asArray( "file.prop1", Integer.class, 314 ),
			Utils.asArray( "file.prop1", int.class, 314 ),
			Utils.asArray( "file.prop1", Long.class, 314L ),
			Utils.asArray( "file.prop1", long.class, 314L ),
			Utils.asArray( "file.prop2", String.class, "value2" ),
			Utils.asArray( "file.prop3", String.class, "value1, value2, value3" ),
			Utils.asArray( "file.prop3", String[].class, Utils.asArray( "value1", "value2", "value3" ) ),

			Utils.asArray( "file.prop1", parameterizedClass( Optional.class, Long.class ), Optional.of( 314L ) ),

			Utils.asArray( "file.prop6", String.class, "1, 2, 3" ),
			Utils.asArray( "file.prop6", String[].class, Utils.asArray( "1", "2", "3" ) ),
			Utils.asArray( "file.prop6", Integer[].class, Utils.asArray( 1, 2, 3 ) ),
			Utils.asArray( "file.prop6", int[].class, new int[] { 1, 2, 3 } ),

			Utils.asArray( "file.prop6", parameterizedClass( Set.class, String.class ), new HashSet<>( asList( "1", "2", "3" ) ) ),
			Utils.asArray( "file.prop6", parameterizedClass( Set.class, Integer.class ), new HashSet<>( asList( 1, 2, 3 ) ) ),

			Utils.asArray( "file.prop6", parameterizedClass( List.class, String.class ), asList( "1", "2", "3" ) ),
			Utils.asArray( "file.prop6", parameterizedClass( List.class, Integer.class ), asList( 1, 2, 3 ) ),

			Utils.asArray( "file.version1", String.class, System.getProperty( "java.version" ) ),
			Utils.asArray( "file.version2", String.class, System.getProperty( "java.version" ) ),
		};
	}

	static private final ConfigRegistry CJ = ConfigRegistry.getInstance();
	static private final ConvertersRegistry CR = CJ.converters();

	private final String prop;
	private final Type type;
	private final Object expected;

	public ConfigJavaTest( String prop, Type type, Object expected )
	{
		this.prop = prop;
		this.type = type;
		this.expected = expected;
	}

	@Before
	public void setUp()
	{
		ConfigProviderResolver.setInstance( null );
		ConfigRegistry.reset();
		ConfigRegistry.getInstance().filterSource( cs -> cs.value().startsWith( "file" ) );
	}

	@Test
	public void run()
	{
		final Object o = CR.getConverter( this.type ).create( CJ.root().getNode( this.prop ), 0 );

		assertThat( o, is( this.expected ) );
	}

}
