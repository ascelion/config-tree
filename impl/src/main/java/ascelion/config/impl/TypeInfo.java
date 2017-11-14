//
//package ascelion.config.impl;
//
//import java.lang.reflect.Type;
//import java.util.Map;
//import java.util.TreeMap;
//
//import static java.lang.String.format;
//
//final class TypeInfo implements Type
//{
//
//	static TypeInfo create( Type type )
//	{
//		final Map<Type, TypeInfo> types = new TreeMap<>( ( t1, t2 ) -> t1.getTypeName().compareTo( t2.getTypeName() ) );
//
//		return get( types, type );
//	}
//
//	static private TypeInfo get( Map<Class<?>, TypeInfo> types, Type type )
//	{
//		return types.computeIfAbsent( type, t -> new TypeInfo( types, t ) );
//	}
//
//	private final Type type;
//	private final TypeInfo sci;
//	private final TypeInfo[] ifs;
//
//	private TypeInfo( Map<Type, TypeInfo> types, Type type )
//	{
//		types.put( type, this );
//
//		this.type = type;
//
//		if( type instanceof Class ) {
//			final Class<?> cls = this.sci = ( sc != null && sc != Object.class ) ? new ClassInfo( sc ) : null;
//		}
//	}
//
//	@Override
//	public String toString()
//	{
//		return format( "{%s}", this.type.toString() );
//	}
//}
