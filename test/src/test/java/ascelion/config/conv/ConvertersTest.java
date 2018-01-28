
package ascelion.config.conv;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import ascelion.config.api.ConfigConverter;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
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

	static class Derived21 extends Derived2
	{

		Derived21( ConfigConverter<?> cvt )
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

	private static Field cachedField;

	@BeforeClass
	static public void setUpClass() throws NoSuchFieldException
	{
		cachedField = Converters.class.getDeclaredField( "cached" );

		cachedField.setAccessible( true );
	}

	private final Converters cvs = new Converters();
	private Map<Class<?>, ConfigConverter<?>> cached;

	@Before
	public void setUp() throws IllegalAccessException
	{
		this.cached = (Map<Class<?>, ConfigConverter<?>>) cachedField.get( this.cvs );
	}

	@Test
	public void registerBaseOnly()
	{
		final ConfigConverter<Base> cb = new BaseCVT();

		final int count = this.cached.size();

		this.cvs.register( cb );

		assertThat( this.cached.size(), is( count + 1 ) );

		final Base ob = (Base) this.cvs.getConverter( Base.class ).create( "" );
		final Base o1 = (Base) this.cvs.getConverter( Derived1.class ).create( "" );
		final Base o2 = (Base) this.cvs.getConverter( Derived2.class ).create( "" );
		final Base o21 = (Base) this.cvs.getConverter( Derived21.class ).create( "" );

		assertThat( this.cached.size(), is( count + 4 ) );

		assertThat( ob, is( instanceOf( Base.class ) ) );
		assertThat( o1, is( instanceOf( Base.class ) ) );
		assertThat( o2, is( instanceOf( Base.class ) ) );
		assertThat( o21, is( instanceOf( Base.class ) ) );

		assertThat( ob.cvt, is( sameInstance( cb ) ) );
		assertThat( o1.cvt, is( sameInstance( cb ) ) );
		assertThat( o2.cvt, is( sameInstance( cb ) ) );
		assertThat( o21.cvt, is( sameInstance( cb ) ) );
	}

	@Test
	public void registerDerived()
	{
		final ConfigConverter<Base> cb = new BaseCVT();
		final ConfigConverter<Derived1> c1 = new Derived1CVT();
		final ConfigConverter<Derived2> c2 = new Derived2CVT();

		final int count = this.cached.size();

		this.cvs.register( cb );
		this.cvs.register( c1 );
		this.cvs.register( c2 );

		assertThat( this.cached.size(), is( count + 3 ) );

		final Base ob = (Base) this.cvs.getConverter( Base.class ).create( "" );
		final Base o1 = (Base) this.cvs.getConverter( Derived1.class ).create( "" );
		final Base o2 = (Base) this.cvs.getConverter( Derived2.class ).create( "" );
		final Base o21 = (Base) this.cvs.getConverter( Derived21.class ).create( "" );

		assertThat( this.cached.size(), is( count + 4 ) );

		assertThat( ob, is( instanceOf( Base.class ) ) );
		assertThat( o1, is( instanceOf( Derived1.class ) ) );
		assertThat( o2, is( instanceOf( Derived2.class ) ) );
		assertThat( o21, is( instanceOf( Derived2.class ) ) );

		assertThat( ob.cvt, is( sameInstance( cb ) ) );
		assertThat( o1.cvt, is( sameInstance( c1 ) ) );
		assertThat( o2.cvt, is( sameInstance( c2 ) ) );
		assertThat( o21.cvt, is( sameInstance( c2 ) ) );
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
