
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigSource;
import ascelion.config.conv.ConverterRegistry;

import static ascelion.config.conv.Utils.asArray;
import static ascelion.config.conv.Utils.asSet;
import static io.leangen.geantyref.GenericTypeReflector.getTypeParameter;
import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@ConfigSource( "file.properties" )
@ConfigSource( "file.conf" )
@ConfigSource( "file.ini" )
@ConfigSource( "file.yml" )
@RunWith( Parameterized.class )
public class InterfaceLinkTest
{

	static private final String PREFIX = "file.interfaces";
	static private final String PROP1 = PREFIX + ".1";
	static private final String PROP2 = "${" + PREFIX + ".2}";

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
			map( parameterizedClass( InterfaceIntPA.class ), new int[] { 0 }, new int[] { 1 }, new int[] { 2, 3, 4 }, new int[] { 5, 6, 7 } ),
			map( parameterizedClass( InterfaceLongA.class ), new Long[] { 0L }, new Long[] { 1L }, new Long[] { 2L, 3L, 4L }, new Long[] { 5L, 6L, 7L } ),
			map( parameterizedClass( InterfaceDoubleL.class ), asList( 0D ), asList( 1D ), asList( 2D, 3D, 4D ), asList( 5D, 6D, 7D ) ),
			map( parameterizedClass( InterfaceStringS.class ), asSet( "0" ), asSet( "1" ), asSet( "2", "3", "4" ), asSet( "5", "6", "7" ) ),
		};
	}

	static private final ConfigJava CJ = new ConfigJava();
	static private final ConverterRegistry CR = ConverterRegistry.instance();

	@BeforeClass
	static public void setUpClass()
	{
		ConfigProviderResolver.setInstance( null );
		ConfigRegistry.setInstance( null );
		ConfigRegistry.getInstance().filterSource( cs -> cs.value().startsWith( "file" ) );
	}

	private final Type type;
	private final Class<?> iType;
	private final Map<String, ?> expected;

	public InterfaceLinkTest( Type type, Map<String, ?> expected )
	{
		this.type = type;
		this.iType = (Class<?>) getTypeParameter( type, Map.class.getTypeParameters()[1] );
		this.expected = expected;
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
		final Object o = CR.getConverter( this.type ).create( n, 2 );

		assertThat( o, is( instanceOf( Map.class ) ) );

		final Map<String, ?> m = (Map<String, ?>) o;

		assertThat( m.size(), is( 4 ) );

		this.expected.forEach( ( k, v ) -> {
			final Object t = m.get( k );

			assertThat( t, is( instanceOf( this.iType ) ) );
			assertThat( k, ( (Interface<?>) t ).values(), is( v ) );
		} );
	}
}
