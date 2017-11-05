//
//package ascelion.cdi.conf;
//
//import java.lang.reflect.Array;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.Collection;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.BiFunction;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import ascelion.shared.cdi.conf.ConfigNode;
//import ascelion.shared.cdi.conf.ConfigValue;
//
//import static java.lang.String.format;
//import static org.apache.commons.lang3.StringUtils.isBlank;
//import static org.apache.commons.lang3.StringUtils.isNotBlank;
//
//import com.google.common.primitives.Primitives;
//
//final class Converter
//{
//
//	static String[] split( String value )
//	{
//		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
//	}
//
//	private final ConfigNode root;
//	private final ConfigValue anno;
//	private final BiFunction<Class<?>, String, ?> func;
//	private final Type type;
//
//	Converter( ConfigNode root, ConfigValue anno, BiFunction<Class<?>, String, ?> func, Type type )
//	{
//		this.root = root;
//		this.anno = anno;
//		this.func = func;
//		this.type = type;
//	}
//
//	private Converter( Converter parent, Type type )
//	{
//		this.root = parent.root;
//		this.anno = parent.anno;
//		this.func = parent.func;
//		this.type = type;
//	}
//
//	Object convert()
//	{
//		if( this.type instanceof Class ) {
//			Class<?> cls = (Class<?>) this.type;
//
//			if( cls.isArray() ) {
//				final Class<?> ct = cls.getComponentType();
//				final Converter cv = new Converter( this, ct );
//				final String[] sv = split( ExpressionRules.eval( this.anno.value(), this.root ) );
//
//				return Stream.of( sv )
//					.map( x -> this.func.apply( ct, x ) )
//					.toArray( n -> (Object[]) Array.newInstance( ct, n ) );
//			}
//			else {
//				String v = ExpressionRules.eval( this.anno.value(), this.root );
//
//				if( cls.isPrimitive() ) {
//					cls = Primitives.wrap( cls );
//
//					if( isBlank( v ) ) {
//						v = "0";
//					}
//				}
//
//				if( isBlank( v ) ) {
//					return null;
//				}
//
//				return this.func.apply( cls, v );
//			}
//		}
//		else {
//			final ParameterizedType p = (ParameterizedType) this.type;
//			final Class<?> o0 = (Class<?>) p.getActualTypeArguments()[0];
//
//			if( Collection.class.isAssignableFrom( (Class) p.getRawType() ) ) {
//				final String[] sv = split( ExpressionRules.eval( this.anno.value(), this.root ) );
//
//				if( p.getRawType() == Set.class ) {
//					return Stream.of( sv ).map( x -> this.func.apply( o0, x ) ).collect( Collectors.toSet() );
//				}
//				else {
//					return Stream.of( sv ).map( x -> this.func.apply( o0, x ) ).collect( Collectors.toList() );
//				}
//			}
//			if( p.getRawType() == Map.class ) {
//				if( o0 != String.class ) {
//					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
//				}
//
//				final Type t1 = p.getActualTypeArguments()[1];
//
//				if( !( t1 instanceof Class ) ) {
//					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
//				}
//
//				final Class<?> o1 = (Class<?>) t1;
//
//				if( o1 == Object.class ) {
//					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
//				}
//
//				final ConfigNode n = this.root.getNode( this.anno.value() );
//
//				if( n == null ) {
//					return null;
//				}
//
//				return n.asMap( this.anno.unwrap(), x -> convertTo( f, o1, x ) );
//			}
//		}
//		return null;
//	}
//}
