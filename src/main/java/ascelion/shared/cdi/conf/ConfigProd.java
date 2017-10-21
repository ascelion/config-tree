
package ascelion.shared.cdi.conf;

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

		final ConfigValue a = getValueAnnotation( ip );
		final InstanceInfo<? extends BiFunction> c = this.converters.computeIfAbsent( a.converter(), x -> {
			// shouldn't happen
			throw new IllegalStateException( format( "Cannot find converter of type %s", x.getName() ) );
		} );

		return convert( ip, c.instance );
	}

	private <T> T convert( InjectionPoint ip, BiFunction<Class<?>, String, T> f )
	{
		final Type t = ip.getType();
		final ConfigNode n = getConfigNode( ip );
		final ConfigValue a = getValueAnnotation( ip );
		final String s = n != null ? n.getItem() : null;

		if( t instanceof Class ) {
			return convertTo( f, t, s );
		}
		else {
			final ParameterizedType p = (ParameterizedType) t;
			final Class<?> o0 = (Class<?>) p.getActualTypeArguments()[0];

			if( Collection.class.isAssignableFrom( (Class) p.getRawType() ) ) {
				if( p.getRawType() == Set.class ) {
					return (T) Stream.of( expandValues( s ) ).map( x -> f.apply( o0, x ) ).collect( Collectors.toSet() );
				}
				else {
					return (T) Stream.of( expandValues( s ) ).map( x -> f.apply( o0, x ) ).collect( Collectors.toList() );
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

				if( n == null ) {
					return null;
				}

				int u = a.unwrap();
				String b = this.cc.getRoot().getPath();

				while( u-- > 0 ) {
					final int x = b.indexOf( '.' );

					if( x < 0 ) {
						break;
					}

					b = b.substring( x + 1 );
				}

				return (T) n.asMap( b, x -> convertTo( f, o1, x ) );
			}

			throw new UnsupportedOperationException( format( "Cannot inject field of type %s", t ) );
		}
	}

	private <T> T convertTo( BiFunction<Class<?>, String, T> f, Type t, String v )
	{
		Class<?> c = (Class<?>) t;

		if( c.isArray() ) {
			final Class<?> o = c.getComponentType();

			return (T) Stream.of( expandValues( v ) )
				.map( x -> f.apply( o, x ) )
				.collect( Collectors.toList() )
				.toArray( (Object[]) Array.newInstance( o, 0 ) );
		}
		else {
			if( c.isPrimitive() ) {
				c = Primitives.wrap( c );

				if( isBlank( v ) ) {
					v = "0";
				}
			}
			if( isBlank( v ) ) {
				return null;
			}

			return f.apply( c, expandValue( v ) );
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
