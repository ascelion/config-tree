
package ascelion.config.cvt;

import java.lang.reflect.Constructor;
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

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.impl.Utils;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static ru.vyarus.java.generics.resolver.util.TypeToStringUtils.toStringType;

public final class Converters
{

	static final TypeVariable<? extends Class<?>> CV_TYPE = ConfigConverter.class.getTypeParameters()[0];

	static private final String[] CREATE = { "valueOf", "parse", "create", "from", "fromValue" };

	private final Map<Type, ConfigConverter<?>> cached = new TreeMap<>( Converters::compare );

	private final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();

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

		return toStringType( t1, emptyMap() ).compareTo( toStringType( t2, emptyMap() ) );
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

	private Supplier<ConfigNode> root;

	public Converters()
	{
		put( Enum.class, ( t, u, x ) -> Enum.valueOf( (Class) t, u ) );

		add( Class.class, ExtraConverters::createClass );

		add( Boolean.class, ExtraConverters::createBoolean );
		add( Byte.class, Byte::parseByte );
		add( Short.class, Short::parseShort );
		add( Integer.class, Integer::parseInt );
		add( Long.class, Long::parseLong );
		add( Float.class, Float::parseFloat );
		add( Double.class, Double::parseDouble );

		add( boolean.class, ExtraConverters::createBoolean );
		add( byte.class, Byte::parseByte );
		add( short.class, Short::parseShort );
		add( int.class, Integer::parseInt );
		add( long.class, Long::parseLong );
		add( float.class, Float::parseFloat );
		add( double.class, Double::parseDouble );

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

		add( new ArrayConverter.IntArray( this::getValue ) );
		add( new ArrayConverter.LongArray( this::getValue ) );
		add( new ArrayConverter.DoubleArray( this::getValue ) );
		add( new ArrayConverter.StringArray( this::getValue ) );

		add( new ListConverter.IntList( this::getValue ) );
		add( new ListConverter.LongList( this::getValue ) );
		add( new ListConverter.DoubleList( this::getValue ) );
		add( new ListConverter.StringList( this::getValue ) );

		add( new SetConverter.IntSet( this::getValue ) );
		add( new SetConverter.LongSet( this::getValue ) );
		add( new SetConverter.DoubleSet( this::getValue ) );
		add( new SetConverter.StringSet( this::getValue ) );
	}

	public void setRootNode( ConfigNode root )
	{
		this.root = () -> root;
	}

	public void setRootNode( Supplier<ConfigNode> root )
	{
		this.root = root;
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
			put( t, c );
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
					put( t, s.get() );
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

	public <T> T getValue( Type t, ConfigNode u )
	{
		return getValue( t, u, 0 );
	}

	public <T> T getValue( Type t, ConfigNode u, int unwrap )
	{
		return (T) getConverter( t ).create( t, u, unwrap );
	}

	public <T> T getValue( Type t, String u )
	{
		return getValue( t, u, 0 );
	}

	public <T> T getValue( Type t, String u, int unwrap )
	{
		return (T) getConverter( t ).create( t, u, unwrap );
	}

	ConfigNode node( String path )
	{
		if( this.root == null || this.root.get() == null ) {
			throw new IllegalStateException( "No configuration provided to Converters" );
		}

		return this.root.get().getNode( path );
	}

	private <T> ConfigConverter<T> getConverter( Type type )
	{
		final Lock rdLock = this.RW_LOCK.readLock();

		rdLock.lock();

		try {
			if( this.cached.containsKey( type ) ) {
				return (ConfigConverter<T>) this.cached.get( type );
			}

			rdLock.unlock();

			try {
				final Lock wrLock = this.RW_LOCK.writeLock();

				wrLock.lock();

				try {
					if( this.cached.containsKey( type ) ) {
						return (ConfigConverter<T>) this.cached.get( type );
					}

					ConfigConverter<T> c = this.cached.entrySet().stream()
						.filter( e -> isBaseOf( e.getKey(), type ) )
						.map( e -> (ConfigConverter<T>) e.getValue() )
						.findFirst()
						.orElse( null );

					if( c != null ) {
						put( type, c );

						return c;
					}

					c = inferConverter( type );

					put( type, c );

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

	private <T> ConfigConverter<T> inferConverter( Type type )
	{
		if( type instanceof Class ) {
			final Class<?> cls = (Class<?>) type;

			try {
				final Constructor<?> c = cls.getConstructor( String.class );

				return ( t, u, x ) -> {
					try {
						return (T) c.newInstance( u );
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

					return ( t, u, x ) -> {
						try {
							return (T) m.invoke( null, u );
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
		if( type instanceof ParameterizedType ) {
			final ParameterizedType pt = (ParameterizedType) type;
			final Type rt = pt.getRawType();

			if( rt.equals( Map.class ) ) {
				return new MapConverter( pt.getActualTypeArguments()[1], this );
			}

			return getConverter( rt );
		}
		if( type instanceof Class<?> && ( (Class) type ).isInterface() ) {
			return new InterfaceConverter<>( this );
		}

		throw new ConfigException( format( "NO WAY to construct a %s", type.getTypeName() ) );
	}

	private void add( Type type, Function<String, ?> func )
	{
		put( type, ( t, u, x ) -> func.apply( u ) );
	}

	private void put( Type type, ConfigConverter<?> conv )
	{
		if( type instanceof Class<?> && ( (Class) type ).isPrimitive() ) {
			this.cached.put( type, PrimitiveConverter.wrap( conv ) );
		}
		else if( conv.isNullHandled() ) {
			this.cached.put( type, conv );
		}
		else {
			this.cached.put( type, NullableConverter.wrap( conv ) );
		}
	}

	private <T> void add( ConfigConverter<T> c )
	{
		final Class<? extends ConfigConverter> cls = c.getClass();
		final Type t = Utils.converterType( cls );

		if( t == null ) {
			throw new IllegalArgumentException( format( "No type information for converter %s", cls.getName() ) );
		}

		put( t, c );
	}
}
