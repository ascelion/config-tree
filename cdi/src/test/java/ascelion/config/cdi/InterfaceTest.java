
package ascelion.config.cdi;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import javax.enterprise.inject.spi.CDI;

import ascelion.config.api.ConfigSource;
import ascelion.config.api.ConfigValue;
import ascelion.config.conv.DataSourceDefinition;
import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasses( {
	InterfaceTest.Bean1.class,
//	InterfaceTest.Bean2.class,
} )
@ConfigSource( "interfaces.yml" )
@UseConfigExtension
public class InterfaceTest
{

	static class Bean1
	{

		@ConfigValue( "${databases.db1}" )
		private DataSourceDefinition def;
	}

//	static class Bean2
//	{
//
//		@ConfigValue( value = "databases", unwrap = 1 )
//		private Map<String, DataSourceDefinition> defs;
//	}

	@Test
	public void run1()
	{
		final Bean1 bean = CDI.current().select( Bean1.class ).get();

		assertThat( bean, is( notNullValue() ) );

		assertThat( bean.def, is( notNullValue() ) );

		assertThat( bean.def.getProperties(), is( notNullValue() ) );
		assertThat( bean.def.getProperties().get( "prop1" ), is( "value1" ) );
		assertThat( bean.def.getProperties().get( "prop2" ), is( "value2" ) );
		assertThat( bean.def.values(), is( new Integer[] { 10, 20, 30 } ) );

		Stream.of( DataSourceDefinition.class.getMethods() )
			.forEach( m -> {
				try {
					System.out.printf( "%s: %s\n", m.getName(), m.invoke( bean.def ) );
				}
				catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
					fail( e.getMessage() );
				}
			} );
		;
	}

//	@Test
//	public void run2()
//	{
//		final Bean2 bean = CDI.current().select( Bean2.class ).get();
//
//		assertThat( bean, is( notNullValue() ) );
//
//		assertThat( bean.defs, is( notNullValue() ) );
//
//		final DataSourceDefinition def1 = bean.defs.get( "db1" );
//
//		assertThat( def1, is( notNullValue() ) );
//		assertThat( def1.getProperties().get( "prop1" ), is( "value1" ) );
//		assertThat( def1.getProperties().get( "prop2" ), is( "value2" ) );
//
//		Stream.of( DataSourceDefinition.class.getMethods() )
//			.forEach( m -> {
//				try {
//					System.out.printf( "%s: %s\n", m.getName(), m.invoke( def1 ) );
//				}
//				catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
//					fail( e.getMessage() );
//				}
//			} );
//		;
//
//		final DataSourceDefinition def2 = bean.defs.get( "db2" );
//
//		assertThat( def2, is( notNullValue() ) );
//
//		Stream.of( DataSourceDefinition.class.getMethods() )
//			.forEach( m -> {
//				try {
//					System.out.printf( "%s: %s\n", m.getName(), m.invoke( def2 ) );
//				}
//				catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
//					fail( e.getMessage() );
//				}
//			} );
//		;
//	}
}
