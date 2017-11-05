
package ascelion.cdi.conf;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
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

import ascelion.shared.cdi.conf.ConfigValue;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Typed( ConfigProd.class )
class ConfigProd extends ConfigProdBase
{

	static private final Logger L = LoggerFactory.getLogger( ConfigProd.class );

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
		final BiFunction<Class<?>, String, ?> f = this.converters.computeIfAbsent( a.converter(), x -> {
			// shouldn't happen
			throw new IllegalStateException( format( "Cannot find converter of type %s", x.getName() ) );
		} ).instance;

		final Type t = ip.getType();

//		return new Converter( this.cc.root(), a, f, t ).convert();
//		final String s = configItem( ip, a );

		if( t instanceof Class ) {
			return convertTo( f, t, a.value() );
		}
		else {
			final ParameterizedType p = (ParameterizedType) t;
			final Class<?> o0 = (Class<?>) p.getActualTypeArguments()[0];

			if( Collection.class.isAssignableFrom( (Class) p.getRawType() ) ) {
				final String[] s = splitValues( Eval.eval( a.value(), this.cc.root() ) );

				if( p.getRawType() == Set.class ) {
					return Stream.of( s ).map( x -> f.apply( o0, x ) ).collect( Collectors.toSet() );
				}
				else {
					return Stream.of( s ).map( x -> f.apply( o0, x ) ).collect( Collectors.toList() );
				}
			}
			if( p.getRawType() == Map.class ) {
				if( o0 != String.class ) {
					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", t ) );
				}

				final Type t1 = p.getActualTypeArguments()[1];

				if( !( t1 instanceof Class ) ) {
					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", t ) );
				}

				final Class<?> o1 = (Class<?>) t1;

				if( o1 == Object.class ) {
					throw new UnsupportedOperationException( format( "Cannot inject field of type %s", t ) );
				}

				final ConfigNodeImpl n = configNode( a );

				if( n == null ) {
					return null;
				}

				return n.asMap( a.unwrap(), x -> convertTo( f, o1, x ) );
			}

			throw new UnsupportedOperationException( format( "Cannot inject field of type %s", t ) );
		}
	}

	private <T> T convertTo( BiFunction<Class<?>, String, T> f, Type t, String v )
	{
		Class<?> c = (Class<?>) t;

		if( c.isArray() ) {
			final Class<?> o = c.getComponentType();
			final String[] s = splitValues( Eval.eval( v, this.cc.root() ) );

			return (T) Stream.of( v )
				.map( x -> f.apply( o, x ) )
				.toArray( n -> (Object[]) Array.newInstance( o, n ) );
		}
		else {
			String s = Eval.eval( v, this.cc.root() );

			if( c.isPrimitive() ) {
				c = Primitives.wrap( c );

				if( isBlank( v ) ) {
					s = "0";
				}
			}
			if( isBlank( s ) ) {
				return null;
			}

			return f.apply( c, s );
		}
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
