
package ascelion.shared.cdi.conf;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;
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
import org.apache.deltaspike.core.util.BeanUtils;

@ApplicationScoped
@Typed( ConfigProd.class )
class ConfigProd extends ConfigProdBase
{

	@Inject
	private BeanManager bm;

	@Produces
	@Dependent
	@ConfigValue( "unused" )
	Object create( InjectionPoint ip )
	{
		String val = getProperty( ip );
		Type t = ip.getType();

		if( val == null ) {
			if( t instanceof Class ) {
				final Class c = (Class) t;

				if( c.isPrimitive() ) {
					t = Primitives.wrap( c );
					val = "0";
				}
			}
		}
		if( val == null ) {
			return null;
		}

		final ConfigValue ano = BeanUtils.extractAnnotation( ip.getAnnotated(), ConfigValue.class );
		final Set<Bean<?>> beans = this.bm.getBeans( ano.converter() );

		if( beans.size() > 0 ) {
			final Bean<BiFunction> bean = (Bean<BiFunction>) this.bm.resolve( beans );
			final CreationalContext<BiFunction> cc = this.bm.createCreationalContext( bean );
			final BiFunction cv = (BiFunction) this.bm.getReference( bean, ano.converter(), cc );

			try {
				return convert( cv, t, val );
			}
			finally {
				bean.destroy( cv, cc );
			}
		}
		else {
			try {
				return convert( ano.converter().newInstance(), t, val );
			}
			catch( InstantiationException | IllegalAccessException e ) {
				throw new RuntimeException( e );
			}
		}
	}

	private <T> T convert( BiFunction<Class<?>, String, T> cv, Type type, String val )
	{
		if( type instanceof Class ) {
			return cv.apply( (Class<?>) type, val );
		}
		else {
			final String[] v = val.split( "\\s*[,;]\\s*" );
			final ParameterizedType p = (ParameterizedType) type;
			final Class<?> c = (Class<?>) p.getActualTypeArguments()[0];

			if( p.getRawType() == Set.class ) {
				return (T) Stream.of( v ).map( x -> cv.apply( c, x ) ).collect( Collectors.toSet() );
			}
			else {
				return (T) Stream.of( v ).map( x -> cv.apply( c, x ) ).collect( Collectors.toList() );
			}
		}
	}

}
