//
//package ascelion.config.impl;
//
//import java.lang.reflect.Array;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Proxy;
//import java.lang.reflect.Type;
//import java.util.Collection;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import ascelion.config.api.ConfigConverter;
//import ascelion.config.api.ConfigNode;
//import ascelion.config.api.ConfigValue;
//
//import static java.lang.String.format;
//import static org.apache.commons.lang3.StringUtils.isBlank;
//
//import com.google.common.primitives.Primitives;
//
//class TypedValue
//{
//
//	final ConfigNode root;
//	final ConfigValue anno;
//	final Type type;
//	final Function<Class<? extends ConfigConverter>, ConfigConverter> conv;
//
//	TypedValue( ConfigNode root, ConfigValue anno, Type type, Function<Class<? extends ConfigConverter>, ConfigConverter> conv )
//	{
//		this.root = root;
//		this.anno = anno;
//		this.type = type;
//		this.conv = conv;
//	}
//
//	Object get()
//	{
//		if( this.type instanceof Class ) {
//			return get( (Class<?>) this.type );
//		}
//		else {
//			final ParameterizedType p0;
//			final Class<?> r0;
//			final Class<?> t00;
//
//			try {
//				p0 = (ParameterizedType) this.type;
//				r0 = (Class<?>) p0.getRawType();
//				t00 = (Class<?>) p0.getActualTypeArguments()[0];
//			}
//			catch( final ClassCastException e ) {
//				throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
//			}
//
//			if( Collection.class.isAssignableFrom( r0 ) ) {
//				if( p0.getRawType() == Set.class ) {
//					return streamOf( t00 ).collect( Collectors.toSet() );
//				}
//				else {
//					return streamOf( t00 ).collect( Collectors.toList() );
//				}
//			}
//			if( p0.getRawType() == Map.class ) {
//				if( t00 != String.class ) {
//					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
//				}
//
//				final ConfigNode n = this.root.getNode( this.anno.value() );
//
//				if( n == null ) {
//					return null;
//				}
//
//				final Type t01 = p0.getActualTypeArguments()[1];
//
//				if( t01 instanceof Class && ( (Class) t01 ).isInterface() && t01 != Collection.class ) {
//					return n.getNodes().stream()
//						.collect( Collectors.toMap( c -> {
//							return c.getName();
//						}, c -> {
//							final ConfigValueLiteral a = new ConfigValueLiteral( c.getPath(), this.anno.converter(), this.anno.unwrap() );
//
//							return new TypedValue( this.root, a, t01, this.conv ).get();
//						} ) );
//				}
//				else {
//					return n.asMap( this.anno.unwrap(), v -> {
//						if( v.contains( Eval.Token.S_BEG ) ) {
//							final ConfigValueLiteral a = new ConfigValueLiteral( v, this.anno.converter(), this.anno.unwrap() );
//
//							return new TypedValue( this.root, a, t01, this.conv ).get();
//						}
//						else {
//							return v;
//						}
//					} );
//				}
//			}
//		}
//
//		throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
//	}
//
//	Object get( Class<?> type )
//	{
//		if( type.isArray() ) {
//			final Class<?> ct = type.getComponentType();
//			final Stream<?> st = streamOf( ct );
//
//			if( ct.isPrimitive() ) {
//				final Stream<Number> ns = st.map( Number.class::cast );
//
//				if( ct == int.class ) {
//					return ns.mapToInt( Number::intValue ).toArray();
//				}
//				if( ct == long.class ) {
//					return ns.mapToLong( Number::longValue ).toArray();
//				}
//				if( ct == double.class ) {
//					return ns.mapToDouble( Number::doubleValue ).toArray();
//				}
//
//				throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
//			}
//			return st
//				.toArray( n -> {
//					return (Object[]) Array.newInstance( ct, n );
//				} );
//		}
//
//		if( type.isInterface() ) {
//			final Class[] types = new Class[] { type };
//			final InvocationHandler han = new InterfaceValue( this.root, this.anno.value(), type, this.conv );
//
//			return Proxy.newProxyInstance( Thread.currentThread().getContextClassLoader(), types, han );
//		}
//
//		String v = eval();
//
//		if( type.isPrimitive() ) {
//			type = Primitives.wrap( type );
//
//			if( isBlank( v ) ) {
//				v = "0";
//			}
//		}
//
//		if( isBlank( v ) ) {
//			return null;
//		}
//
//		return this.conv.apply( this.anno.converter() ).create( type, v );
//	}
//
//	private Stream<?> streamOf( Class<?> cls )
//	{
//		final String[] sv = Utils.toArray( eval() );
//
//		return Stream.of( sv ).map( x -> this.conv.apply( this.anno.converter() ).create( cls, x ) );
//	}
//
//	private String eval()
//	{
//		return Eval.eval( this.anno.value(), this.root );
//	}
//}
