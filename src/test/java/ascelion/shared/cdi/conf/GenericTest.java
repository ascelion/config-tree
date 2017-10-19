
package ascelion.shared.cdi.conf;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ascelion.tests.cdi.CdiUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.array;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import org.jglue.cdiunit.AdditionalClasses;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( CdiUnit.class )
@AdditionalClasses( {
	GenericTest.IBean.class,
	GenericTest.LBean.class,
	GenericTest.SBean.class,
} )
@UseConfigExtension
public class GenericTest
{

	static {
		System.setProperty( "prop1", "10" );
		System.setProperty( "prop2", "20" );
		System.setProperty( "prop3", "30" );
		System.setProperty( "props", "${prop1}, ${prop2}, ${prop3}" );
	}

	static class BaseBean<T>
	{

		T value0;

		BaseBean( T value0 )
		{
			this.value0 = value0;
		}

		@ConfigValue( "prop1" )
		T value1;

		T value2;

		T value3;

		public void set( @ConfigValue( "prop2" ) T value2, @ConfigValue( "prop3" ) T value3 )
		{
			this.value2 = value2;
			this.value3 = value3;
		}

		@ConfigValue( "props" )
		List<T> values;

		@ConfigValue( "props" )
		T[] valuev;
	}

	static class IBean extends BaseBean<Integer>
	{

		@ConfigValue( "prop1" )
		long xValue1;

		IBean( @ConfigValue( "prop0:-2" ) int value0 )
		{
			super( value0 );
		}
	}

	static class LBean extends BaseBean<Long>
	{

		@ConfigValue( "prop1" )
		String xValue1;

		LBean( @ConfigValue( "prop0:-4" ) long value0 )
		{
			super( value0 );
		}
	}

	static class SBean extends BaseBean<String>
	{

		@ConfigValue( "prop1" )
		int xValue1;

		SBean( @ConfigValue( "prop0:-6" ) String value0 )
		{
			super( value0 );
		}
	}

	@Inject
	private IBean iBean;

	@Inject
	private LBean lBean;

	@Inject
	private SBean sBean;

	@ConfigValue( "props" )
	private Integer[] nValues;

	@ConfigValue( "props" )
	private Map<String, Integer[]> mnValues;

	@ConfigValue( "props" )
	private Map<String, String[]> msValues;

	@ConfigValue( "props" )
	private Map<String, String> msValue;

	@Test
	public void run()
	{
		assertThat( this.iBean, is( notNullValue() ) );
		assertThat( this.iBean.value0, is( -2 ) );
		assertThat( this.iBean.value1, is( 10 ) );
		assertThat( this.iBean.value2, is( 20 ) );
		assertThat( this.iBean.valuev, is( array( equalTo( 10 ), equalTo( 20 ), equalTo( 30 ) ) ) );
		assertThat( this.iBean.xValue1, is( 10L ) );

		assertThat( this.lBean, is( notNullValue() ) );
		assertThat( this.lBean.value0, is( -4L ) );
		assertThat( this.lBean.value1, is( 10L ) );
		assertThat( this.lBean.value2, is( 20L ) );
		assertThat( this.lBean.valuev, is( array( equalTo( 10L ), equalTo( 20L ), equalTo( 30L ) ) ) );
		assertThat( this.lBean.xValue1, is( "10" ) );

		assertThat( this.sBean, is( notNullValue() ) );
		assertThat( this.sBean.value0, is( "-6" ) );
		assertThat( this.sBean.xValue1, is( 10 ) );
		assertThat( this.sBean.value1, is( "10" ) );
		assertThat( this.sBean.value1, is( "10" ) );
		assertThat( this.sBean.value2, is( "20" ) );
		assertThat( this.sBean.valuev, is( array( equalTo( "10" ), equalTo( "20" ), equalTo( "30" ) ) ) );
		assertThat( this.sBean.xValue1, is( 10 ) );

		assertThat( this.mnValues, hasEntry( "props", new Integer[] { 10, 20, 30 } ) );
		assertThat( this.msValues, hasEntry( "props", new String[] { "10", "20", "30" } ) );
		assertThat( this.nValues, is( array( equalTo( 10 ), equalTo( 20 ), equalTo( 30 ) ) ) );
	}
}
