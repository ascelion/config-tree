
package ascelion.shared.cdi.conf;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import com.google.common.primitives.Primitives;

@ApplicationScoped
@Typed( ConfigProd.class )
class ConfigProd extends ConfigProdBase
{

	@Inject
	private BeanManager bm;

	@Produces
	@Dependent
	@ConfigValue( "" )
	Object create( InjectionPoint ip )
	{
		Object val = getProperty( ip );
		Type t = ip.getType();
		boolean p = false;

		if( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
			t = Primitives.wrap( (Class<?>) t );
			p = true;
		}
		if( val == null && p ) {
			val = "0";
		}
		if( val == null ) {
			return null;
		}

		final ConfigValue ano = getAnnotation( ip );

		val = convert( ano.converter(), t, val );

		if( val instanceof Map && ano.unwrap().length() > 0 ) {
			final Map<String, Object> o = (Map<String, Object>) val;
			final Map<String, Object> m = new TreeMap<>();

			o.forEach( ( k, v ) -> m.put( k.substring( ano.unwrap().length() + 1 ), v ) );

			val = m;
		}

		return val;
	}

	private Object convert( final Class<? extends BiFunction> cls, Type t, Object val )
	{
		final Set<Bean<?>> beans = this.bm.getBeans( cls );

		if( beans.size() > 0 ) {
			final Bean<BiFunction> bean = (Bean<BiFunction>) this.bm.resolve( beans );
			final CreationalContext<BiFunction> cc = this.bm.createCreationalContext( bean );
			final BiFunction cv = (BiFunction) this.bm.getReference( bean, cls, cc );

			try {
				return convert( cv, t, val );
			}
			finally {
				bean.destroy( cv, cc );
			}
		}
		else {
			try {
				return convert( cls.newInstance(), t, val );
			}
			catch( InstantiationException | IllegalAccessException e ) {
				throw new RuntimeException( e );
			}
		}
	}

	private <T> T convert( BiFunction<Class<?>, String, T> cv, Type type, Object val )
	{
		if( type instanceof Class ) {
			return cv.apply( (Class<?>) type, (String) val );
		}
		else {
			final ParameterizedType p = (ParameterizedType) type;
			final Class<?> c = (Class<?>) p.getActualTypeArguments()[0];

			if( p.getRawType() == Map.class ) {
				return (T) val;
			}

			final String[] v = ( (String) val ).split( "\\s*[,;]\\s*" );

			if( p.getRawType() == Set.class ) {
				return (T) Stream.of( v ).map( x -> cv.apply( c, x ) ).collect( Collectors.toSet() );
			}
			else {
				return (T) Stream.of( v ).map( x -> cv.apply( c, x ) ).collect( Collectors.toList() );
			}
		}
	}

}
