
package ascelion.config.conv;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;

import static ascelion.config.conv.NullableConverter.nullable;
import static ascelion.config.conv.PrimitiveConverter.primitive;
import static io.leangen.geantyref.GenericTypeReflector.getTypeName;
import static io.leangen.geantyref.GenericTypeReflector.getTypeParameter;
import static java.lang.String.format;

public final class Converters implements ConfigConverter<Object>
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

			return t1.getTypeName().compareTo( t2.getTypeName() );
		}
		if( t1 instanceof Class ) {
			return -1;
		}
		if( t2 instanceof Class ) {
			return +1;
		}

		return getTypeName( t1 ).compareTo( getTypeName( t2 ) );
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
		put( Enum.class, nullable( ( t, u ) -> Enum.valueOf( (Class) t, u ) ) );

		addNullable( Class.class, ExtraConverters::createClass );

		addNullable( Boolean.class, ExtraConverters::createBoolean );
		addNullable( Byte.class, Byte::parseByte );
		addNullable( Short.class, Short::parseShort );
		addNullable( Integer.class, Integer::parseInt );
		addNullable( Long.class, Long::parseLong );
		addNullable( Float.class, Float::parseFloat );
		addNullable( Double.class, Double::parseDouble );

		addPrimitive( boolean.class, ExtraConverters::createBoolean );
		addPrimitive( byte.class, Byte::parseByte );
		addPrimitive( short.class, Short::parseShort );
		addPrimitive( int.class, Integer::parseInt );
		addPrimitive( long.class, Long::parseLong );
		addPrimitive( float.class, Float::parseFloat );
		addPrimitive( double.class, Double::parseDouble );

		add( String.class, u -> u );

		addNullable( Duration.class, Duration::parse );
		addNullable( Instant.class, Instant::parse );
		addNullable( LocalDate.class, LocalDate::parse );
		addNullable( LocalDateTime.class, LocalDateTime::parse );
		addNullable( LocalTime.class, LocalTime::parse );
		addNullable( OffsetTime.class, OffsetTime::parse );
		addNullable( OffsetDateTime.class, OffsetDateTime::parse );

		addNullable( URL.class, ExtraConverters::createURL );

		add( int[].class, ExtraConverters::createIntA );
		add( long[].class, ExtraConverters::createLongA );
		add( double[].class, ExtraConverters::createDoubleA );
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

	@Override
	public Object create( Type t, ConfigNode u, int unwrap )
	{
		return getConverter( t ).create( t, u, unwrap );
	}

	@Override
	public Object create( Type t, String u )
	{
		return getConverter( t ).create( t, u );
	}

	ConfigNode node( String path )
	{
		if( this.root == null || this.root.get() == null ) {
			throw new IllegalStateException( "No configuration provided to Converters" );
		}

		return this.root.get().getNode( path );
	}

	ConfigConverter<?> getConverter( Type type )
	{
		final Lock rdLock = this.RW_LOCK.readLock();

		rdLock.lock();

		try {
			if( this.cached.containsKey( type ) ) {
				return this.cached.get( type );
			}

			rdLock.unlock();

			try {
				final Lock wrLock = this.RW_LOCK.writeLock();

				wrLock.lock();

				try {
					if( this.cached.containsKey( type ) ) {
						return this.cached.get( type );
					}

					ConfigConverter<?> c = this.cached.entrySet().stream()
						.filter( e -> isBaseOf( e.getKey(), type ) )
						.map( e -> (ConfigConverter<?>) e.getValue() )
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

	private ConfigConverter<?> inferConverter( Type type )
	{
		if( type instanceof ParameterizedType ) {
			final ParameterizedType pt = (ParameterizedType) type;
			final Type rt = pt.getRawType();

			if( rt.equals( Set.class ) ) {
				final Type ct = pt.getActualTypeArguments()[0];
				return new SetConverter( ct, getConverter( ct ) );
			}
			if( rt.equals( List.class ) ) {
				final Type ct = pt.getActualTypeArguments()[0];
				return new ListConverter( ct, getConverter( ct ) );
			}
			if( rt.equals( Map.class ) ) {
				final Type ct = pt.getActualTypeArguments()[1];
				return new MapConverter( ct, getConverter( ct ) );
			}

			return getConverter( rt );
		}
		if( type instanceof GenericArrayType ) {
			final GenericArrayType at = (GenericArrayType) type;
			final Type ct = at.getGenericComponentType();

			return new ArrayConverter( ct, getConverter( ct ) );
		}
		if( type instanceof Class ) {
			final Class<?> cls = (Class<?>) type;

			if( cls.isArray() ) {
				final Class<?> ct = cls.getComponentType();

				return new ArrayConverter( ct, getConverter( ct ) );
			}
			if( cls.isInterface() ) {
				return new InterfaceConverter<>( this );
			}

			try {
				final Constructor<?> c = cls.getConstructor( String.class );

				return ( t, u ) -> {
					try {
						return c.newInstance( u );
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

		throw new ConfigException( format( "NO WAY to construct a %s", type.getTypeName() ) );
	}

	private void add( Type type, Function<String, ?> func )
	{
		put( type, ( t, u ) -> func.apply( u ) );
	}

	private void addNullable( Type type, Function<String, ?> func )
	{
		put( type, nullable( ( t, u ) -> func.apply( u ) ) );
	}

	private void addPrimitive( Type type, Function<String, ?> func )
	{
		put( type, primitive( ( t, u ) -> func.apply( u ) ) );
	}

	private void add( ConfigConverter<?> c )
	{
		final Class<? extends ConfigConverter> cls = c.getClass();
		final Type t = getTypeParameter( cls, CV_TYPE );

		if( t == null ) {
			throw new IllegalArgumentException( format( "No type information for converter %s", cls.getName() ) );
		}

		put( t, c );
	}

	private void put( Type type, ConfigConverter<?> conv )
	{
		if( !this.cached.containsKey( type ) ) {
			if( conv.isNullHandled() ) {
				this.cached.put( type, conv );
			}
			else {
				this.cached.put( type, nullable( conv ) );
			}
		}
	}
}
