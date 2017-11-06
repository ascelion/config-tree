
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

				if( this.root == null ) {
					return null;
				}

				final ConfigNodeImpl n = (ConfigNodeImpl) this.root.getNode( this.anno.value() );

				if( n == null ) {
					return null;
				}

				final Type t01 = p0.getActualTypeArguments()[1];

				return n.asMap( this.anno.unwrap(), v -> {
					final Converter cv = new Converter( v.contains( "${" ) ? this.root : null, new ConfigValueLiteral( v, this.anno.converter(), this.anno.unwrap() ), this.func, t01 );
					final Object vv = cv.convert();

					return vv;
				} );
			}
		}
		return null;
	}

	private Stream<?> streamOf( Class<?> cls )
	{
		final String[] sv = split( eval() );

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
			String v = eval();

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

	private String eval()
	{
		return this.root != null ? Eval.eval( this.anno.value(), this.root ) : this.anno.value();
	}
}
