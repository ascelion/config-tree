
package ascelion.config.convert;

import static io.leangen.geantyref.GenericTypeReflector.getExactReturnType;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigValue;
import ascelion.config.spi.ConfigConverter;
import ascelion.config.spi.ConverterFactory;

import java.beans.Introspector;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

final class InterfaceValue implements InvocationHandler
{

	static private final Set<Method> O_METHODS = unmodifiableSet( new HashSet<>( asList( Object.class.getMethods() ) ) );
	static private final Constructor<MethodHandles.Lookup> LOOKUP;

	static {
		try {
			LOOKUP = MethodHandles.Lookup.class.getDeclaredConstructor( Class.class, int.class );

			LOOKUP.setAccessible( true );
		}
		catch( NoSuchMethodException | SecurityException e ) {
			throw new ExceptionInInitializerError( e );
		}
	}

	private final Map<Method, ConfigValue> names = new HashMap<>();

	private final Class<?> type;
	private final ConfigNode node;
	private final ConverterFactory converters;

	InterfaceValue( Class<?> type, ConfigNode node, ConverterFactory converters )
	{
		this.type = type;
		this.node = node;
		this.converters = converters;

		Stream.of( type.getMethods() )
			.filter( m -> !m.isDefault() )
			.filter( m -> m.getParameterTypes().length == 0 )
			.filter( m -> m.getReturnType() != void.class )
			.forEach( this::addName );
		;
	}

	@Override
	public String toString()
	{
		return format( "%s[%s]", this.type.getSimpleName(), this.node.getPath() );
	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, args );
		}

		if( this.names.containsKey( method ) ) {
			final ConfigValue a = this.names.get( method );
			final Type t = getExactReturnType( method, this.type );
			return this.node
				.getNode( a.value() )
				.flatMap( this.converters.get( t )::convert )
				.orElse( null );
		}
		else if( method.isDefault() ) {
			final Class<?> cls = method.getDeclaringClass();
			final MethodHandle han = LOOKUP.newInstance( cls, -1 )
				.unreflectSpecial( method, cls )
				.bindTo( proxy );

			return han.invokeWithArguments( args );
		}

		throw new RuntimeException( format( "Cannot handle method %s#%s", this.type.getName(), method.getName() ) );
	}

	private void addName( Method m )
	{
		final String name = Introspector.decapitalize( m.getName().replaceAll( "^(is|get)", "" ) );
		final Class<? extends ConfigConverter> conv = ConfigConverter.class;
		final ConfigValue anno = m.getAnnotation( ConfigValue.class );

		this.names.put( m, anno );
	}
}
