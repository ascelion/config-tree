
package ascelion.config.conv;

import java.lang.reflect.Type;
import java.util.List;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ConvertersTest
{

	static class Base
	{

		final ConfigConverter<?> cvt;

		Base( ConfigConverter<?> cvt )
		{
			this.cvt = cvt;
		}
	}

	static class Derived1 extends Base
	{

		Derived1( ConfigConverter<?> cvt )
		{
			super( cvt );
		}
	}

	static class Derived2 extends Base
	{

		Derived2( ConfigConverter<?> cvt )
		{
			super( cvt );
		}
	}

	static class BaseCVT implements ConfigConverter<Base>
	{

		@Override
		public Base create( String u )
		{
			return new Base( this );
		}
	}

	static class Derived1CVT implements ConfigConverter<Derived1>
	{

		@Override
		public Derived1 create( String u )
		{
			return new Derived1( this );
		}

		@Override
		public boolean isNullHandled()
		{
			return true;
		}
	}

	static class Derived2CVT implements ConfigConverter<Derived2>
	{

		@Override
		public Derived2 create( String u )
		{
			return new Derived2( this );
		}

		@Override
		public boolean isNullHandled()
		{
			return true;
		}
	}

	interface Interface1
	{
	}

	interface Interface2
	{
	}

	interface DerivedInterface1 extends Interface1
	{
	}

	interface DerivedInterface2 extends Interface2
	{
	}

	interface DerivedInterface extends Interface1, Interface2
	{
	}

	private final Converters cvs = new Converters();

	@Test( expected = ConfigException.class )
	public void unknown()
	{
		this.cvs.register( new BaseCVT() );

		this.cvs.getConverter( Base.class );
		this.cvs.getConverter( Derived1.class );
	}

	@Test
	public void registerDerived()
	{
		final ConfigConverter<Base> cb = new BaseCVT();
		final ConfigConverter<Derived1> c1 = new Derived1CVT();
		final ConfigConverter<Derived2> c2 = new Derived2CVT();

		final int count = this.cvs.getConverters().size();

		this.cvs.register( cb );
		this.cvs.register( c1 );
		this.cvs.register( c2 );

		assertThat( this.cvs.getConverters().size(), is( count + 3 ) );

		final Base ob = (Base) this.cvs.getConverter( Base.class ).create( "" );
		final Base o1 = (Base) this.cvs.getConverter( Derived1.class ).create( "" );
		final Base o2 = (Base) this.cvs.getConverter( Derived2.class ).create( "" );

		assertThat( this.cvs.getConverters().size(), is( count + 3 ) );

		assertThat( ob, is( instanceOf( Base.class ) ) );
		assertThat( o1, is( instanceOf( Derived1.class ) ) );
		assertThat( o2, is( instanceOf( Derived2.class ) ) );

		assertThat( ob.cvt, is( sameInstance( cb ) ) );
		assertThat( o1.cvt, is( sameInstance( c1 ) ) );
		assertThat( o2.cvt, is( sameInstance( c2 ) ) );
	}

	@Test
	public void interfaces()
	{
		final int count = this.cvs.getConverters().size();

		final ConfigConverter<?> ci1 = this.cvs.getConverter( Interface1.class );
		final ConfigConverter<?> ci2 = this.cvs.getConverter( Interface2.class );
		final ConfigConverter<?> cdi1 = this.cvs.getConverter( DerivedInterface1.class );
		final ConfigConverter<?> cdi2 = this.cvs.getConverter( DerivedInterface2.class );
		final ConfigConverter<?> cdi = this.cvs.getConverter( DerivedInterface.class );

		assertThat( ci1, is( not( ci2 ) ) );
		assertThat( ci1, is( not( cdi1 ) ) );
		assertThat( ci1, is( not( cdi2 ) ) );
		assertThat( ci1, is( not( cdi ) ) );

		assertThat( this.cvs.getConverters().size(), is( count + 5 ) );
	}

	@Test
	public void intA()
	{
		final int[] values = (int[]) this.cvs.getConverter( int[].class ).create( "1, 2, 3, 4" );

		assertThat( values, is( new int[] { 1, 2, 3, 4 } ) );
	}

	@Test
	public void longA()
	{
		final long[] values = (long[]) this.cvs.getConverter( long[].class ).create( "1, 2, 3, 4" );

		assertThat( values, is( new long[] { 1, 2, 3, 4 } ) );
	}

	@Test
	public void doubleA()
	{
		final double[] values = (double[]) this.cvs.getConverter( double[].class ).create( "1, 2, 3, 4" );

		assertThat( values, is( new double[] { 1, 2, 3, 4 } ) );
	}

	@Test
	public void intList()
	{
		final Type type = parameterizedClass( List.class, Integer.class );

		final Object values = this.cvs.getConverter( type ).create( "1, 2, 3, 4" );

		assertThat( values, is( asList( 1, 2, 3, 4 ) ) );
	}

	@Test
	public void longList()
	{
		final Type type = parameterizedClass( List.class, Long.class );

		final Object values = this.cvs.getConverter( type ).create( "1, 2, 3, 4" );

		assertThat( values, is( asList( 1L, 2L, 3L, 4L ) ) );
	}

	@Test
	public void intArray()
	{
		final Object values = this.cvs.getConverter( Integer[].class ).create( "1, 2, 3, 4" );

		assertThat( values, is( new Integer[] { 1, 2, 3, 4 } ) );
	}

}
