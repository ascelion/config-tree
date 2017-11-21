
package ascelion.config.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.primitives.Primitives;

public class Converters implements ConfigConverter<Object>
{

	static final TypeVariable<? extends Class<?>> CV_TYPE = ConfigConverter.class.getTypeParameters()[0];

	static private final String[] CREATE = { "valueOf", "parse", "create", "from", "fromValue" };

	private final Map<Type, ConfigConverter<?>> cached = new TreeMap<>( Converters::compare );

	private final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();

	static private String toString( Type type )
	{
		if( type instanceof Class<?> ) {
			return ( (Class) type ).getName();
		}
		if( type instanceof ParameterizedType ) {
			final StringBuilder b = new StringBuilder();
			final ParameterizedType t = (ParameterizedType) type;

			b.append( toString( t.getRawType() ) );
			b.append( Stream.of( t.getActualTypeArguments() ).map( Converters::toString ).collect( Collectors.joining( ", ", "<", ">" ) ) );

			return b.toString();
		}
		if( type instanceof GenericArrayType ) {
			final StringBuilder b = new StringBuilder();
			final GenericArrayType t = (GenericArrayType) type;

			b.append( toString( t.getGenericComponentType() ) );
			b.append( "[]" );

			return b.toString();
		}

		throw new UnsupportedOperationException( type.getClass().getName() );
	}

	static private int compare( Type t1, Type t2 )
	{
		if( t1.equals( t2 ) ) {
			return 0;
		}

		if( t1 instanceof Class && t2 instanceof Class ) {
			if( isBaseOf( t2, t1 ) ) {
				return -1;
			}
			if( isBaseOf( t1, t2 ) ) {
				return +1;
			}
		}
		if( t1 instanceof Class ) {
			return -1;
		}
		if( t2 instanceof Class ) {
			return +1;
		}

		return toString( t1 ).compareTo( toString( t2 ) );
	}

	static private boolean isBaseOf( Type t1, Type t2 )
	{
		if( t1 instanceof Class && t2 instanceof Class ) {
			final Class<?> c1 = (Class<?>) t1;
			final Class<?> c2 = (Class<?>) t2;
			return c1.isAssignableFrom( c2 );
		}
		else {
			return false;
		}
	}

	public Converters()
	{
		put( Enum.class, ( t, u ) -> Enum.valueOf( (Class) t, u ) );

		add( Class.class, ExtraConverters::createClass );
		add( String.class, u -> u );

		add( Duration.class, Duration::parse );
		add( Instant.class, Instant::parse );
		add( LocalDate.class, LocalDate::parse );
		add( LocalDateTime.class, LocalDateTime::parse );
		add( LocalTime.class, LocalTime::parse );
		add( OffsetTime.class, OffsetTime::parse );
		add( OffsetDateTime.class, OffsetDateTime::parse );

		add( URL.class, ExtraConverters::createURL );

		add( int[].class, ExtraConverters::createIntA );
		add( long[].class, ExtraConverters::createLongA );
		add( double[].class, ExtraConverters::createDoubleA );

		add( new ArrayConverter.IntArray( self() ) );
		add( new ArrayConverter.LongArray( self() ) );
		add( new ArrayConverter.DoubleArray( self() ) );
		add( new ArrayConverter.StringArray( self() ) );

		add( new ListConverter.IntList( self() ) );
		add( new ListConverter.LongList( self() ) );
		add( new ListConverter.DoubleList( self() ) );
		add( new ListConverter.StringList( self() ) );

		add( new SetConverter.IntSet( self() ) );
		add( new SetConverter.LongSet( self() ) );
		add( new SetConverter.DoubleSet( self() ) );
		add( new SetConverter.StringSet( self() ) );
	}

	public void register( ConfigConverter<?> c )
	{
		final Lock wrLock = this.RW_LOCK.writeLock();

		wrLock.lock();

		try {
			add( c );
		}
		finally {
			wrLock.unlock();
		}
	}

	public void register( Type t, ConfigConverter<?> c )
	{
		final Lock wrLock = this.RW_LOCK.writeLock();

		wrLock.lock();

		try {
			this.cached.put( t, c );
		}
		finally {
			wrLock.unlock();
		}
	}

	public void register( Type t, Supplier<ConfigConverter<?>> s )
	{
		final Lock rdLock = this.RW_LOCK.readLock();

		rdLock.lock();

		try {
			if( this.cached.containsKey( t ) ) {
				return;
			}

			rdLock.unlock();

			try {
				final Lock wrLock = this.RW_LOCK.writeLock();

				wrLock.lock();

				try {
					this.cached.put( t, s.get() );
				}
				finally {
					wrLock.unlock();
				}
			}
			finally {
				rdLock.lock();
			}
		}
		finally {
			rdLock.unlock();
		}
	}

	@Override
	public Object create( Type t, String u )
	{
		if( t instanceof Class ) {
			final Class<?> c = (Class<?>) t;

			if( c.isPrimitive() ) {
				t = Primitives.wrap( c );

				if( isBlank( u ) ) {
					u = "0";
				}
			}
		}
		if( u == null ) {
			return null;
		}

		return getConverter( t ).create( t, u );
	}

	@Override
	public Object create( Class<? super Object> t, String u )
	{
		return getConverter( t ).create( t, u );
	}

	public <T> T getValue( ConfigNode root, Type type, String prop, int unwrap )
	{
		if( type instanceof ParameterizedType ) {
			final ParameterizedType pt = (ParameterizedType) type;
			final Type raw = pt.getRawType();

			if( raw.equals( Map.class ) ) {
				return (T) getMap( root, pt.getActualTypeArguments()[1], prop, unwrap );
			}
		}

		return (T) create( type, root.getValue( prop ) );
	}

	private <T> Map<String, T> getMap( ConfigNode root, Type type, String prop, int unwrap )
	{
		final ConfigNode node = root.getNode( prop );
		final Map<String, T> m = new TreeMap<>();

		node.asMap( unwrap ).forEach( ( k, v ) -> m.put( k, (T) create( type, v ) ) );

		return m;
	}

	<X> ConfigConverter<X> self()
	{
		return (ConfigConverter<X>) this;
	}

	private ConfigConverter<Object> getConverter( Type type )
	{
		final Lock rdLock = this.RW_LOCK.readLock();

		rdLock.lock();

		try {
			if( this.cached.containsKey( type ) ) {
				return (ConfigConverter<Object>) this.cached.get( type );
			}

			rdLock.unlock();

			try {
				final Lock wrLock = this.RW_LOCK.writeLock();

				wrLock.lock();

				try {
					if( this.cached.containsKey( type ) ) {
						return (ConfigConverter<Object>) this.cached.get( type );
					}

					ConfigConverter<Object> c = this.cached.entrySet().stream()
						.filter( e -> isBaseOf( e.getKey(), type ) )
						.map( e -> (ConfigConverter<Object>) e.getValue() )
						.findFirst()
						.orElse( null );

					if( c != null ) {
						this.cached.put( type, c );

						return c;
					}

					c = inferConverter( type );

					this.cached.put( type, c );

					return c;
				}
				finally {
					wrLock.unlock();
				}
			}
			finally {
				rdLock.lock();
			}
		}
		finally {
			rdLock.unlock();
		}
	}

	private ConfigConverter<Object> inferConverter( Type type )
	{
		if( type instanceof Class ) {
			final Class<?> cls = (Class<?>) type;

			try {
				final Constructor<?> c = cls.getConstructor( String.class );

				return ( t, u ) -> {
					try {
						return (Object) c.newInstance( u );
					}
					catch( InstantiationException | IllegalAccessException e ) {
						throw new ConfigException( u, e.getCause() );
					}
					catch( final InvocationTargetException e ) {
						throw new ConfigException( u, e.getCause() );
					}
				};
			}
			catch( final NoSuchMethodException e ) {
			}

			for( final String create : CREATE ) {
				try {
					final Method m = cls.getMethod( create, String.class );

					if( !Modifier.isStatic( m.getModifiers() ) ) {
						continue;
					}

					return ( t, u ) -> {
						try {
							return m.invoke( null, u );
						}
						catch( final IllegalAccessException e ) {
							throw new ConfigException( u, e );
						}
						catch( final InvocationTargetException e ) {
							throw new ConfigException( u, e.getCause() );
						}
					};
				}
				catch( final NoSuchMethodException e ) {
				}
			}
		}

		throw new ConfigException( format( "NO WAY to construct a %s from a string", type.getTypeName() ) );
	}

	private <X> void add( Class<X> type, Function<String, X> func )
	{
		put( type, ( t, u ) -> func.apply( u ) );
	}

	private <X> void put( Class<X> type, ConfigConverter<X> conv )
	{
		final Class<X> wrap = Primitives.wrap( type );

		if( wrap != type ) {
			this.cached.put( wrap, conv );
		}

		this.cached.put( type, conv );
	}

	private void add( ConfigConverter<?> c )
	{
		final Class<? extends ConfigConverter> cls = c.getClass();
		final Type t = Utils.converterType( cls );

		if( t == null ) {
			throw new IllegalArgumentException( format( "No type information for converter %s", cls.getName() ) );
		}

		this.cached.put( t, c );
	}

}
