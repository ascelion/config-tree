//
//package ascelion.config.conv;
//
//import java.lang.reflect.Type;
//import java.util.List;
//
//import ascelion.config.api.ConfigConverter;
//import ascelion.config.impl.Utils;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;
//
//import org.junit.Test;
//
//public class ParamTypeTest
//{
//
//	static abstract class Base<A, B> implements Comparable<A>, ConfigConverter<B>
//	{
//
//		@Override
//		public B create( Type t, String u )
//		{
//			return null;
//		}
//
//		@Override
//		public int compareTo( A o )
//		{
//			return 0;
//		}
//	}
//
//	static class Derived1<N> extends Base<Integer, N>
//	{
//	}
//
//	static class Derived11 extends Derived1<Long>
//	{
//	}
//
//	static class Derived2<N> extends Base<Integer, List<N>>
//	{
//	}
//
//	static class Derived21 extends Derived2<Long>
//	{
//	}
//
//	@Test
//	public void run11()
//	{
//		final Type at = Utils.paramType( Derived11.class, Derived1.class, 0 );
//
//		System.out.println( at );
//
//		assertThat( at, is( (Type) Long.class ) );
//	}
//
//	@Test
//	public void run21()
//	{
//		final Type at = Utils.paramType( Derived21.class, Base.class, 1 );
//		final TypeRef<List<Long>> ref = new TypeRef<List<Long>>()
//		{
//		};
//
//		System.out.println( at );
//
//		assertThat( at, is( ref.type() ) );
//	}
//
//}
