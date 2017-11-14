
package ascelion.config.impl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import ascelion.config.api.ConfigConverter;

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
		public Base create( Class<? super Base> t, String u )
		{
			if( t == Base.class ) {
				return new Base( this );
			}
			if( (Class) t == Derived1.class ) {
				return new Derived1( this );
			}
			if( (Class) t == Derived2.class ) {
				return new Derived2( this );
			}
			if( (Class) t == Derived21.class ) {
				return new Derived21( this );
			}

			throw new UnsupportedOperationException();
		}
	}

	static class Derived1CVT implements ConfigConverter<Derived1>
	{

		@Override
		public Derived1 create( Class<? super Derived1> t, String u )
		{
			return new Derived1( this );
		}
	}

	static class Derived2CVT implements ConfigConverter<Derived2>
	{

		@Override
		public Derived2 create( Class<? super Derived2> t, String u )
		{
			return new Derived2( this );
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

		final Base ob = (Base) this.cvs.create( Base.class, null );
		final Base o1 = (Base) this.cvs.create( Derived1.class, null );
		final Base o2 = (Base) this.cvs.create( Derived2.class, null );
		final Base o21 = (Base) this.cvs.create( Derived21.class, null );

		assertThat( this.cached.size(), is( count + 4 ) );

		assertThat( ob, is( instanceOf( Base.class ) ) );
		assertThat( o1, is( instanceOf( Derived1.class ) ) );
		assertThat( o2, is( instanceOf( Derived2.class ) ) );
		assertThat( o21, is( instanceOf( Derived21.class ) ) );

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

		final Base ob = (Base) this.cvs.create( Base.class, null );
		final Base o1 = (Base) this.cvs.create( Derived1.class, null );
		final Base o2 = (Base) this.cvs.create( Derived2.class, null );
		final Base o21 = (Base) this.cvs.create( Derived21.class, null );

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
		final int[] values = (int[]) this.cvs.create( int[].class, "1, 2, 3, 4" );

		assertThat( values, is( new int[] { 1, 2, 3, 4 } ) );
	}

	@Test
	public void longA()
	{
		final long[] values = (long[]) this.cvs.create( long[].class, "1, 2, 3, 4" );

		assertThat( values, is( new long[] { 1, 2, 3, 4 } ) );
	}

	@Test
	public void doubleA()
	{
		final double[] values = (double[]) this.cvs.create( double[].class, "1, 2, 3, 4" );

		assertThat( values, is( new double[] { 1, 2, 3, 4 } ) );
	}

	@Test
	public void intList()
	{
		final TypeRef<List<Integer>> ref = new TypeRef<List<Integer>>()
		{
		};

		final Object values = this.cvs.create( ref.type(), "1, 2, 3, 4" );

		assertThat( values, is( asList( 1, 2, 3, 4 ) ) );
	}

	@Test
	public void longList()
	{
		final TypeRef<List<Long>> ref = new TypeRef<List<Long>>()
		{
		};

		final Object values = this.cvs.create( ref.type(), "1, 2, 3, 4" );

		assertThat( values, is( asList( 1L, 2L, 3L, 4L ) ) );
	}

	@Test
	public void intArray()
	{
		final Object values = this.cvs.create( Integer[].class, "1, 2, 3, 4" );

		assertThat( values, is( new Integer[] { 1, 2, 3, 4 } ) );
	}

}
