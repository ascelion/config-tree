//
//package ascelion.config.impl;
//
//import java.beans.Introspector;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Type;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Function;
//import java.util.stream.Stream;
//
//import ascelion.config.api.ConfigConverter;
//import ascelion.config.api.ConfigNode;
//import ascelion.config.api.ConfigValue;
//
//import static ascelion.config.impl.Utils.methodsOf;
//import static ascelion.config.impl.Utils.path;
//import static java.lang.String.format;
//import static org.apache.commons.lang3.StringUtils.isNotBlank;
//
//final class InterfaceValue implements InvocationHandler
//{
//
//	static final Set<Method> O_METHODS = methodsOf( Object.class );
//
//	private final ConfigNode root;
//	private final String name;
//	private final Map<Method, ConfigValue> names = new HashMap<>();
//	private final Class<?> type;
//	private final Function<Class<? extends ConfigConverter>, ConfigConverter> conv;
//
//	InterfaceValue( ConfigNode root, String name, Class<?> type, Function<Class<? extends ConfigConverter>, ConfigConverter> conv )
//	{
//		this.root = root;
//		this.name = name;
//		this.type = type;
//		this.conv = conv;
//
//		Stream.of( type.getMethods() )
//			.filter( m -> m.getParameterTypes().length == 0 )
//			.filter( m -> m.getReturnType() != void.class )
//			.forEach( this::addName );
//		;
//	}
//
//	@Override
//	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
//	{
//		if( O_METHODS.contains( method ) ) {
//			return method.invoke( this, args );
//		}
//
//		if( this.names.containsKey( method ) ) {
//			final ConfigValue a = this.names.get( method );
//			final Type g = method.getGenericReturnType();
//			final Class<?> t = method.getReturnType();
//			final TypedValue c;
//
//			if( t.isInterface() && Collection.class.isAssignableFrom( t ) || Map.class.isAssignableFrom( t ) ) {
//				c = new TypedValue( this.root, a, g, this.conv );
//			}
//			else {
//				c = new TypedValue( this.root, a, t, this.conv );
//			}
//
//			return c.get();
//		}
//
//		throw new NoSuchMethodError( format( "%s#%s", this.type.getName(), method.getName() ) );
//	}
//
//	private void addName( Method m )
//	{
//		String name = Introspector.decapitalize( m.getName().replaceAll( "^(is|get)", "" ) );
//		Class<? extends ConfigConverter> conv = ConfigConverter.class;
//		ConfigValue anno = m.getAnnotation( ConfigValue.class );
//		int unwrap = 0;
//
//		if( anno != null ) {
//			if( isNotBlank( anno.value() ) ) {
//				name = anno.value();
//			}
//
//			conv = anno.converter();
//			unwrap = anno.unwrap();
//		}
//
//		anno = new ConfigValueLiteral( path( this.name, name ), conv, unwrap );
//
//		this.names.put( m, anno );
//	}
//
//	@Override
//	public String toString()
//	{
//		return format( "%s[%s]", this.type.getSimpleName(), this.name );
//	}
//}
