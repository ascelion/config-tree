
package ascelion.cdi.conf;

import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import static ascelion.cdi.conf.Utils.methodsOf;
import static ascelion.cdi.conf.Utils.path;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Typed( ConfigProd.class )
class ConfigProd extends ConfigProdBase
{

	static private final Logger L = LoggerFactory.getLogger( ConfigProd.class );
	static private final Set<Method> O_METHODS = methodsOf( Object.class );

	static String[] split( String value )
	{
		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}

	class Converter
	{

		final ConfigValue anno;
		final Type type;
		final BiFunction<Class<?>, String, ?> conv;

		Converter( ConfigValue anno, Type type, BiFunction<Class<?>, String, ?> conv )
		{
			this.anno = anno;
			this.type = type;
			this.conv = conv;
		}

		Object get()
		{
			if( this.type instanceof Class ) {
				return get( (Class<?>) this.type );
			}
			else {
				final ParameterizedType p0;
				final Class<?> r0;
				final Class<?> t00;

				try {
					p0 = (ParameterizedType) this.type;
					r0 = (Class<?>) p0.getRawType();
					t00 = (Class<?>) p0.getActualTypeArguments()[0];
				}
				catch( final ClassCastException e ) {
					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
				}

				if( Collection.class.isAssignableFrom( r0 ) ) {
					if( p0.getRawType() == Set.class ) {
						return streamOf( t00 ).collect( Collectors.toSet() );
					}
					else {
						return streamOf( t00 ).collect( Collectors.toList() );
					}
				}
				if( p0.getRawType() == Map.class ) {
					if( t00 != String.class ) {
						throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
					}

					final ConfigNodeImpl n = ConfigProd.this.cc.root().getNode( this.anno.value() );

					if( n == null ) {
						return null;
					}

					final Type t01 = p0.getActualTypeArguments()[1];

					if( t01 instanceof Class && ( (Class) t01 ).isInterface() && t01 != Collection.class ) {
						return n.getNodes().stream()
							.collect( Collectors.toMap( c -> {
								return c.getName();
							}, c -> {
								final ConfigValueLiteral a = new ConfigValueLiteral( c.getPath(), this.anno.converter(), this.anno.unwrap() );

								return new Converter( a, t01, this.conv ).get();
							} ) );
					}
					else {
						return n.asMap( this.anno.unwrap(), v -> {
							if( v.contains( Eval.Token.S_BEG ) ) {
								final ConfigValueLiteral a = new ConfigValueLiteral( v, this.anno.converter(), this.anno.unwrap() );

								return new Converter( a, t01, this.conv ).get();
							}
							else {
								return v;
							}
						} );
					}
				}
			}

			throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
		}

		Object get( Class<?> type )
		{
			if( type.isArray() ) {
				final Class<?> ct = type.getComponentType();
				final Stream<?> st = streamOf( ct );

				if( ct.isPrimitive() ) {
					final Stream<Number> ns = st.map( Number.class::cast );

					if( ct == int.class ) {
						return ns.mapToInt( Number::intValue ).toArray();
					}
					if( ct == long.class ) {
						return ns.mapToLong( Number::longValue ).toArray();
					}
					if( ct == double.class ) {
						return ns.mapToDouble( Number::doubleValue ).toArray();
					}

					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
				}
				return st
					.toArray( n -> {
						return (Object[]) Array.newInstance( ct, n );
					} );
			}
			if( type.isInterface() ) {
				final Class[] types = new Class[] { type };
				final InvocationHandler han = new InterfaceHandler( this.anno.value(), type );

				return Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(), types, han );
			}

			String v = eval();

			if( type.isPrimitive() ) {
				type = Primitives.wrap( type );

				if( isBlank( v ) ) {
					v = "0";
				}
			}

			if( isBlank( v ) ) {
				return null;
			}

			return this.conv.apply( type, v );
		}

		private Stream<?> streamOf( Class<?> cls )
		{
			final String[] sv = split( eval() );

			return Stream.of( sv ).map( x -> this.conv.apply( cls, x ) );
		}

		private String eval()
		{
			return Eval.eval( this.anno.value(), ConfigProd.this.cc.root() );
		}
	}

	class InterfaceHandler implements InvocationHandler
	{

		private final String name;
		private final Map<Method, ConfigValue> names = new HashMap<>();
		private final Class<?> type;

		InterfaceHandler( String name, Class<?> type )
		{
			this.name = name;
			this.type = type;

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
				final Type g = method.getGenericReturnType();
				final Class<?> t = method.getReturnType();
				final Converter c;

				if( t.isInterface() && Collection.class.isAssignableFrom( t ) || Map.class.isAssignableFrom( t ) ) {
					c = new Converter( a, g, getConverter( a.converter() ) );
				}
				else {
					c = new Converter( a, t, getConverter( a.converter() ) );
				}

				return c.get();
			}

			throw new NoSuchMethodError( method.getName() );
		}

		private void addName( Method m )
		{
			String name = Introspector.decapitalize( m.getName().replaceAll( "^(is|get)", "" ) );
			Class<? extends BiFunction> conv = DefaultCVT.class;
			ConfigValue anno = m.getAnnotation( ConfigValue.class );
			int unwrap = 0;

			if( anno != null ) {
				if( isNotBlank( anno.value() ) ) {
					name = anno.value();
				}
				if( anno.converter() != BiFunction.class ) {
					conv = anno.converter();
				}

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

	@Inject
	private BeanManager bm;

	@Inject
	private ConfigExtension ext;

	private final Map<Class<? extends BiFunction>, InstanceInfo<? extends BiFunction>> converters = new IdentityHashMap<>();

	@Produces
	@Dependent
	@ConfigValue( "" )
	Object create( InjectionPoint ip )
	{
		L.trace( "Value: {}", ip.getAnnotated() );

		final ConfigValue a = annotation( ip );
		final BiFunction<Class<?>, String, ?> c = getConverter( a.converter() );
		final Type t = ip.getType();
		final Object v = new Converter( a, t, c ).get();

		return v;
	}

	private BiFunction<Class<?>, String, ?> getConverter( Class<? extends BiFunction> type )
	{
		return this.converters.computeIfAbsent( type, x -> {
			// shouldn't happen
			throw new IllegalStateException( format( "Cannot find converter of type %s", x.getName() ) );
		} ).instance;
	}

	@PostConstruct
	private void postConstruct()
	{
		this.ext.converters().forEach( c -> {
			final Set<Bean<?>> beans = this.bm.getBeans( c );
			final InstanceInfo<BiFunction> info;

			if( beans.size() > 0 ) {
				info = new InstanceInfo<>( this.bm, (Bean<BiFunction>) this.bm.resolve( beans ) );
			}
			else {
				try {
					info = new InstanceInfo<>( c.newInstance() );
				}
				catch( InstantiationException | IllegalAccessException e ) {
					throw new IllegalStateException( e );
				}
			}

			this.converters.put( c, info );
		} );
	}

	@PreDestroy
	private void preDestroy()
	{
		this.converters.values().forEach( InstanceInfo::destroy );
		this.converters.clear();
	}

}
