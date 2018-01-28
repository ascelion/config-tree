
package ascelion.config.conv;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigRegistry;
import ascelion.config.api.ConfigValue;

import static ascelion.config.conv.Utils.methodsOf;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.leangen.geantyref.GenericTypeReflector;

final class InterfaceValue implements InvocationHandler
{

	static final Set<Method> O_METHODS = methodsOf( Object.class );

	private final Map<Method, ConfigValue> names = new HashMap<>();

	private final Class<?> type;
	private final ConfigNode node;

	InterfaceValue( Class<?> type, ConfigNode node )
	{
		this.type = type;
		this.node = node;

		Stream.of( type.getMethods() )
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
			final Type t = GenericTypeReflector.getExactReturnType( method, this.type );

			if( this.node != null ) {
				ConfigNode n = null;

				try {
					n = this.node.getNode( a.value() );
				}
				catch( final ConfigNotFoundException e ) {
				}

				if( n != null ) {
					return getConverter( t ).create( n, a.unwrap() );
				}
			}

			return getConverter( t ).create( null, a.unwrap() );
		}

		throw new NoSuchMethodError( format( "%s#%s", this.type.getName(), method.getName() ) );
	}

	private ConfigConverter<?> getConverter( final Type t )
	{
		return ConfigRegistry.getInstance()
			.converters( this.type.getClassLoader() )
			.getConverter( t );
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

		anno = new ConfigValueLiteral( name, conv, unwrap );

		this.names.put( m, anno );
	}
}
