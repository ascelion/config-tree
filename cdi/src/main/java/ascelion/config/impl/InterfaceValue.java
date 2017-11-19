
package ascelion.config.impl;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigValue;

import static ascelion.config.impl.Utils.methodsOf;
import static ascelion.config.impl.Utils.path;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class InterfaceValue implements InvocationHandler
{

	static final Set<Method> O_METHODS = methodsOf( Object.class );

	private final Map<Method, ConfigValue> names = new HashMap<>();

	private final Class<?> type;
	private final String name;
	private final ConfigNode root;
	private final Converters conv;

	InterfaceValue( Class<?> type, ConfigNode root, String name, Converters conv )
	{
		this.type = type;
		this.name = name;
		this.root = root;
		this.conv = conv;

		Stream.of( type.getMethods() )
			.filter( m -> m.getParameterTypes().length == 0 )
			.filter( m -> m.getReturnType() != void.class )
			.forEach( this::addName );
		;
	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
	{
		if( O_METHODS.contains( method ) ) {
			return method.invoke( this, args );
		}

		if( this.names.containsKey( method ) ) {
			final ConfigValue a = this.names.get( method );

			return this.conv.getValue( this.root, method.getGenericReturnType(), a.value(), a.unwrap() );
		}

		throw new NoSuchMethodError( format( "%s#%s", this.type.getName(), method.getName() ) );
	}

	private void addName( Method m )
	{
		String name = Introspector.decapitalize( m.getName().replaceAll( "^(is|get)", "" ) );
		Class<? extends ConfigConverter> conv = ConfigConverter.class;
		ConfigValue anno = m.getAnnotation( ConfigValue.class );
		int unwrap = 0;

		if( anno != null ) {
			if( isNotBlank( anno.value() ) ) {
				name = anno.value();
			}

			conv = anno.converter();
			unwrap = anno.unwrap();
		}

		anno = new ConfigValueLiteral( path( this.name, name ), conv, unwrap );

		this.names.put( m, anno );
	}

	@Override
	public String toString()
	{
		return format( "%s[%s]", this.type.getSimpleName(), this.name );
	}
}
