
package ascelion.cdi.conf;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigValue;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.primitives.Primitives;

final class Converter
{

	static String[] split( String value )
	{
		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}

	private final ConfigNode root;
	private final ConfigValue anno;
	private final BiFunction<Class<?>, String, ?> func;
	private final Type type;

	Converter( ConfigNode root, ConfigValue anno, BiFunction<Class<?>, String, ?> func, Type type )
	{
		this.root = root;
		this.anno = anno;
		this.func = func;
		this.type = type;
	}

	Object convert()
	{
		if( this.type instanceof Class ) {
			return convertFrom( (Class<?>) this.type );
		}
		else {
			final ParameterizedType p = (ParameterizedType) this.type;
			final Class<?> t0 = (Class<?>) p.getActualTypeArguments()[0];

			if( Collection.class.isAssignableFrom( (Class) p.getRawType() ) ) {
				if( p.getRawType() == Set.class ) {
					return streamOf( t0 ).collect( Collectors.toSet() );
				}
				else {
					return streamOf( t0 ).collect( Collectors.toList() );
				}
			}
			if( p.getRawType() == Map.class ) {
				if( t0 != String.class ) {
					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", this.type ) );
				}

				final ConfigNodeImpl n = (ConfigNodeImpl) this.root.getNode( this.anno.value() );

				if( n == null ) {
					return null;
				}

				final Type t1 = p.getActualTypeArguments()[1];

				return n.asMap( this.anno.unwrap(), x -> {
					final Converter cv = new Converter( this.root, new ConfigValueLiteral( x, this.anno.converter(), this.anno.unwrap() ), this.func, t1 );
					final Object vv = cv.convert();

					return vv;
				} );
			}
		}
		return null;
	}

	private Stream<?> streamOf( Class<?> cls )
	{
		final String[] sv = split( Eval.eval( this.anno.value(), this.root ) );

		return Stream.of( sv ).map( x -> this.func.apply( cls, x ) );
	}

	private Object convertFrom( Class<?> cls )
	{
		if( cls.isArray() ) {
			final Class<?> ct = cls.getComponentType();

			return streamOf( ct )
				.toArray( n -> {
					return (Object[]) Array.newInstance( ct, n );
				} );
		}
		else {
			String v = Eval.eval( this.anno.value(), this.root );

			if( cls.isPrimitive() ) {
				cls = Primitives.wrap( cls );

				if( isBlank( v ) ) {
					v = "0";
				}
			}

			if( isBlank( v ) ) {
				return null;
			}

			return this.func.apply( cls, v );
		}
	}
}
