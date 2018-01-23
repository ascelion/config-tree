
package ascelion.config.conv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigSource;
import ascelion.config.impl.ConfigJava;

import static ascelion.config.impl.Utils.asArray;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.AfterClass;
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
		CJ.addFilter( s -> s.value().startsWith( "interface" ) || "SYS".equals( s.type() ) );
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
		final ConfigNode node = CJ.root().getNode( this.path );
		final Object o = CV.create( DataSourceDefinition.class, node, 0 );

		final DataSourceDefinition dsd = verify( o );

		assertThat( dsd.jndiName(), is( this.jndi ) );
	}

	static DataSourceDefinition verify( Object o )
	{
		assertThat( o, is( notNullValue() ) );
		assertThat( o, is( instanceOf( DataSourceDefinition.class ) ) );

		final DataSourceDefinition dsd = (DataSourceDefinition) o;

		for( final Method m : DataSourceDefinition.class.getMethods() ) {
			try {
				System.out.printf( "%s: %s\n", m.getName(), m.invoke( o ) );
			}
			catch( final IllegalAccessException e ) {
				throw new RuntimeException( m.getName(), e );
			}
			catch( final InvocationTargetException e ) {
				throw new RuntimeException( m.getName(), e.getCause() );
			}
		}
		return dsd;
	}

	@AfterClass
	static public void testMap()
	{
		final Collection<ConfigNode> nodes = CJ.root().getNode( "databases" ).getNodes();

		nodes.forEach( node -> {
			verify( CV.create( DataSourceDefinition.class, node, 0 ) );
		} );
	}
}
