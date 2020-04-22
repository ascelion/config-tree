
package ascelion.config.convert;

import static io.leangen.geantyref.TypeFactory.parameterizedClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ascelion.config.api.ConfigException;
import ascelion.config.core.AbstractTest;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConverterFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ContainersTest extends AbstractTest
{

	private final ConverterFactory converters = new Converters();

	static public Stream<Arguments> maps()
	{
		return Stream.<Arguments> of(
			types( Map.class, HashMap.class ),
			types( SortedMap.class, TreeMap.class ),
			types( LinkedHashMap.class, LinkedHashMap.class ),
			types( IdentityHashMap.class, IdentityHashMap.class ) );
	}

	static public Stream<Arguments> cols()
	{
		return Stream.<Arguments> of(
			types( Collection.class, ArrayList.class ),
			types( List.class, ArrayList.class ),
			types( LinkedList.class, LinkedList.class ),
			types( Set.class, HashSet.class ),
			types( SortedSet.class, TreeSet.class ),
			types( LinkedHashSet.class, LinkedHashSet.class ),
			types( Queue.class, null ) );
	}

	private static <T> Arguments types( Class<T> type, Class<? extends T> expType )
	{
		return Arguments.of( type.getSimpleName(), type, expType );
	}

	@ParameterizedTest( name = "{0}" )
	@MethodSource( "maps" )
	public void map( String unused, Class<? extends Map> mapType, Class<? extends Map> expType )
	{
		final Type type = parameterizedClass( mapType, String.class, Integer.class );

		if( expType == null ) {
			assertThrows( ConfigException.class, () -> {
				this.converters.get( type );
			} );
		}
		else {
			final ConfigConverter<Object> conv = this.converters.get( type );

			assertThat( conv, is( notNullValue() ) );

			final Object cont = this.root.getValue( "map", type ).get();

			assertThat( cont, is( notNullValue() ) );
			assertThat( cont.getClass(), equalTo( expType ) );
		}
	}

	@ParameterizedTest( name = "{0}" )
	@MethodSource( "cols" )
	public void col( String unused, Class<? extends Collection> colType, Class<? extends Collection> expType )
	{
		final Type type = parameterizedClass( colType, Integer.class );

		if( expType == null ) {
			assertThrows( ConfigException.class, () -> {
				this.converters.get( type );
			} );
		}
		else {
			final ConfigConverter<Object> conv = this.converters.get( type );

			assertThat( conv, is( notNullValue() ) );

			final Object cont = this.root.getValue( "col", type ).get();

			assertThat( cont, is( notNullValue() ) );
			assertThat( cont.getClass(), equalTo( expType ) );
		}
	}
}
