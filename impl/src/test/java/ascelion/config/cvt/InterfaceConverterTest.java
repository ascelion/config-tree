
package ascelion.config.cvt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.ConfigJava;

import static ascelion.config.impl.Utils.asArray;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith( Parameterized.class )
@ConfigSource( "interfaces.yml" )
public class InterfaceConverterTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			asArray( "databases.db1", "jdbc/db1" ),
			asArray( "databases.db2", "jdbc/db2" ),
			asArray( "databases.db3", "jdbc/db1" ),
			asArray( "databases.db4", "jdbc/db2" ),
		};
	}

	static private final ConfigJava CJ = new ConfigJava();
	static private final Converters CV = CJ.getConverter();

	@BeforeClass
	static public void setUpClass()
	{
		CJ.add( s -> s.value().startsWith( "interface" ) || "SYS".equals( s.type() ) );
	}

	private final String path;
	private final String jndi;

	public InterfaceConverterTest( String path, String jndi )
	{
		this.path = path;
		this.jndi = jndi;
	}

	@Test
	public void run()
	{
		final ConfigNode node = CJ.root().getNode( "${" + this.path + "}" );
		final Object o = CV.getValue( DataSourceDefinition.class, node );

		assertThat( o, is( notNullValue() ) );
		assertThat( o, is( instanceOf( DataSourceDefinition.class ) ) );

		final DataSourceDefinition dsd = (DataSourceDefinition) o;

		assertThat( dsd.jndiName(), is( this.jndi ) );

		for( final Method m : DataSourceDefinition.class.getMethods() ) {
			try {
				System.out.printf( "%s: %s\n", m.getName(), m.invoke( o ) );
			}
			catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				e.printStackTrace();
			}
		}
	}

//	@AfterClass
//	static public void testMap()
//	{
//		final TypeRef<Map<String, DataSourceDefinition>> ref = new TypeRef<Map<String, DataSourceDefinition>>()
//		{
//		};
//
//		final Object o = CV.getValue( ref.type(), "databases", 0 );
//
//		assertThat( o, is( notNullValue() ) );
//	}
}
