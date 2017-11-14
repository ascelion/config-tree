
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

final class PTI implements ParameterizedType
{

	private Type[] actualTypeArguments;
	private Type rawType;
	private Type ownerType;

	@Override
	public Type[] getActualTypeArguments()
	{
		return this.actualTypeArguments;
	}

	@Override
	public Type getRawType()
	{
		return this.rawType;
	}

	@Override
	public Type getOwnerType()
	{
		return this.ownerType;
	}
}

final class ClassInfo implements Type
{

	static ClassInfo create( Class<?> type )
	{
		final Map<Class<?>, ClassInfo> types = new TreeMap<>( ( t1, t2 ) -> t1.getTypeName().compareTo( t2.getTypeName() ) );

		return get( types, type );
	}

	static private ClassInfo get( Map<Class<?>, ClassInfo> types, Class<?> type )
	{
		return types.computeIfAbsent( type, t -> new ClassInfo( types, t ) );
	}

	private final Class<?> type;
	private final ClassInfo[] ifs;
	private final TypeVariable<?>[] tvs;
	private final Map<TypeVariable, Type> map = new HashMap<>();
	private final ClassInfo sci;
	private final StringBuilder sb = new StringBuilder();

	private ClassInfo( Map<Class<?>, ClassInfo> types, Class<?> type )
	{
		types.put( type, this );

		this.type = type;

		final Class<?> sc = type.getSuperclass();

		this.sb.append( type.getName() );

		this.ifs = Stream.of( type.getInterfaces() ).map( c -> get( types, c ) ).toArray( ClassInfo[]::new );
		this.tvs = type.getTypeParameters();
		if( this.tvs.length > 0 ) {
			this.sb.append( Stream.of( this.tvs ).map( Object::toString ).collect( Collectors.joining( ", ", "<", ">" ) ) );
		}

		this.sci = ( sc != null && sc != Object.class ) ? get( types, sc ) : null;

		final Type gs = type.getGenericSuperclass();

		if( gs instanceof ParameterizedType && this.sci != null ) {
			final ParameterizedType pt = (ParameterizedType) gs;
			final Type[] ata = pt.getActualTypeArguments();

			for( int k = 0; k < ata.length; k++ ) {
//				if( ata[k] instanceof Class ) {
//					this.map.put( this.sci.tvs[k], get( types, (Class<?>) ata[k] ) );
//				}
//				else {
				this.map.put( this.sci.tvs[k], ata[k] );
//				}
			}
		}

		final Type[] gis = type.getGenericInterfaces();

		for( int k = 0; k < gis.length; k++ ) {
			final Type gi = gis[k];

			if( gi instanceof ParameterizedType ) {
				final ParameterizedType pt = (ParameterizedType) gi;
				final Type[] ata = pt.getActualTypeArguments();

				for( int j = 0; j < ata.length; j++ ) {
//					if( ata[j] instanceof Class ) {
//						this.map.put( this.ifs[k].tvs[j], get( types, (Class<?>) ata[j] ) );
//					}
//					else {
					this.map.put( this.ifs[k].tvs[j], ata[j] );
//					}
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return format( "{%s}", this.sb );
	}

	Type actualType( final TypeVariable<?> tv )
	{
		Type t = tv;

		if( this.sci != null ) {
			t = this.sci.actualType( tv );

			if( t == null ) {
				t = tv;
			}
		}
		if( t instanceof TypeVariable ) {
			t = this.map.get( t );
		}

		return t;
	}
}
