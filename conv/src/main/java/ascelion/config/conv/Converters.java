
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
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigException;
import ascelion.config.utils.Utils;

import static ascelion.config.conv.EnumConverter.enumeration;
import static ascelion.config.conv.NullableConverter.nullable;
import static ascelion.config.conv.PrimitiveConverter.primitive;
import static io.leangen.geantyref.GenericTypeReflector.getTypeName;
import static io.leangen.geantyref.GenericTypeReflector.getTypeParameter;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

final class Converters
{

	static final TypeVariable<? extends Class<?>> CV_TYPE = ConfigConverter.class.getTypeParameters()[0];

	static private final String[] CREATE_METHODS = { "valueOf", "parse", "create", "from", "fromValue" };

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

	static class CCH<T>
	{

		final ConfigConverter<T> c;
		final int p;

		CCH( ConfigConverter<T> c )
		{
			this( c, MAX_VALUE );
		}

		CCH( ConfigConverter<T> c, int p )
		{
			this.c = c;
			this.p = p;
		}
	}

	private final Map<Type, CCH<?>> cached = new TreeMap<>( Converters::compare );

	private final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();

	Converters()
	{
		addNullable( Class.class, ExtraConverters::createClass, MAX_VALUE );

		addNullable( Boolean.class, ExtraConverters::createBoolean, MAX_VALUE );
		addNullable( Byte.class, Byte::parseByte, MAX_VALUE );
		addNullable( Short.class, Short::parseShort, MAX_VALUE );
		addNullable( Integer.class, Integer::parseInt, MAX_VALUE );
		addNullable( Long.class, Long::parseLong, MAX_VALUE );
		addNullable( Float.class, Float::parseFloat, MAX_VALUE );
		addNullable( Double.class, Double::parseDouble, MAX_VALUE );

		addPrimitive( boolean.class, ExtraConverters::createBoolean, MAX_VALUE );
		addPrimitive( byte.class, Byte::parseByte, MAX_VALUE );
		addPrimitive( short.class, Short::parseShort, MAX_VALUE );
		addPrimitive( int.class, Integer::parseInt, MAX_VALUE );
		addPrimitive( long.class, Long::parseLong, MAX_VALUE );
		addPrimitive( float.class, Float::parseFloat, MAX_VALUE );
		addPrimitive( double.class, Double::parseDouble, MAX_VALUE );

		add( String.class, u -> u, MAX_VALUE );

		addNullable( Duration.class, Duration::parse, MAX_VALUE );
		addNullable( Instant.class, Instant::parse, MAX_VALUE );
		addNullable( LocalDate.class, LocalDate::parse, MAX_VALUE );
		addNullable( LocalDateTime.class, LocalDateTime::parse, MAX_VALUE );
		addNullable( LocalTime.class, LocalTime::parse, MAX_VALUE );
		addNullable( OffsetTime.class, OffsetTime::parse, MAX_VALUE );
		addNullable( OffsetDateTime.class, OffsetDateTime::parse, MAX_VALUE );

		addNullable( URL.class, ExtraConverters::createURL, MAX_VALUE );

		add( int[].class, ExtraConverters::createIntA, MAX_VALUE );
		add( long[].class, ExtraConverters::createLongA, MAX_VALUE );
		add( double[].class, ExtraConverters::createDoubleA, MAX_VALUE );
	}

	void register( ConfigConverter<?> c )
	{
		final Class<? extends ConfigConverter> cls = c.getClass();
		final Type t = getTypeParameter( cls, CV_TYPE );

		if( t == null ) {
			throw new IllegalArgumentException( format( "No type information for converter %s", cls.getName() ) );
		}

		register( t, c, Utils.getPriority( c ) );
	}

	void register( Type t, ConfigConverter<?> c )
	{
		register( t, c, Utils.getPriority( c ) );
	}

	void register( Type t, ConfigConverter<?> c, int p )
	{
		final Lock wrLock = this.RW_LOCK.writeLock();

		wrLock.lock();

		try {
			put( t, c, p );
		}
		finally {
			wrLock.unlock();
		}
	}

	Map<Type, ConfigConverter<?>> getConverters()
	{
		return this.cached.entrySet().stream()
			.collect( toMap( e -> e.getKey(), e -> e.getValue().c ) );
	}

	ConfigConverter<?> getConverter( Type type )
	{
		final Lock rdLock = this.RW_LOCK.readLock();

		rdLock.lock();

		try {
			if( this.cached.containsKey( type ) ) {
				return this.cached.get( type ).c;
			}

			rdLock.unlock();

			try {
				final Lock wrLock = this.RW_LOCK.writeLock();

				wrLock.lock();

				try {
					if( this.cached.containsKey( type ) ) {
						return this.cached.get( type ).c;
					}

					final CCH<?> h = this.cached.entrySet().stream()
						.filter( e -> isBaseOf( e.getKey(), type ) )
						.map( e -> e.getValue() )
						.findFirst()
						.orElse( null );

					if( h != null ) {
						this.cached.put( type, h );

						return h.c;
					}

					final ConfigConverter<?> c = inferConverter( type );

					put( type, c, MAX_VALUE );

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
				return new SetConverter<>( ct, getConverter( ct ) );
			}
			if( rt.equals( List.class ) ) {
				final Type ct = pt.getActualTypeArguments()[0];
				return new ListConverter<>( ct, getConverter( ct ) );
			}
			if( rt.equals( Map.class ) ) {
				final Type ct = pt.getActualTypeArguments()[1];
				return new MapConverter<>( ct, getConverter( ct ) );
			}
			if( rt.equals( Optional.class ) ) {
				final Type ct = pt.getActualTypeArguments()[0];
				return new OptionalConverter<>( ct, getConverter( ct ) );
			}

			return getConverter( rt );
		}
		if( type instanceof GenericArrayType ) {
			final GenericArrayType at = (GenericArrayType) type;
			final Type ct = at.getGenericComponentType();

			return new ArrayConverter<>( ct, getConverter( ct ) );
		}
		if( type instanceof Class ) {
			final Class<?> cls = (Class<?>) type;

			if( cls.isEnum() ) {
				return enumeration( (Class<? extends Enum>) cls );
			}
			if( cls.isArray() ) {
				final Class<?> ct = cls.getComponentType();

				return new ArrayConverter<>( ct, getConverter( ct ) );
			}
			if( cls.isInterface() ) {
				return new InterfaceConverter<>( cls );
			}

			final ConfigConverter<?> fc = fromClass( cls );

			if( fc != null ) {
				return nullable( fc );
			}
		}

		throw new ConfigException( format( "NO WAY to construct a %s", type.getTypeName() ) );
	}

	private ConfigConverter<?> fromClass( Class<?> cls )
	{
		ConfigConverter<?> c;

		if( ( c = fromConstructor( cls, String.class ) ) != null ) {
			return c;
		}
		if( ( c = fromConstructor( cls, CharSequence.class ) ) != null ) {
			return c;
		}

		for( final String name : CREATE_METHODS ) {
			if( ( c = fromMethod( cls, name, String.class ) ) != null ) {
				return c;
			}
			if( ( c = fromMethod( cls, name, CharSequence.class ) ) != null ) {
				return c;
			}
		}

		return null;
	}

	ConfigConverter<?> fromConstructor( Class<?> cls, Class<?> paramType )
	{
		try {
			final Constructor<?> c = cls.getDeclaredConstructor( paramType );

			c.setAccessible( true );

			return ( u ) -> {
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
			return null;
		}
	}

	ConfigConverter<?> fromMethod( Class<?> cls, String name, Class<?> paramType )
	{
		try {
			final Method m = cls.getDeclaredMethod( name, paramType );

			if( !Modifier.isStatic( m.getModifiers() ) ) {
				return null;
			}

			m.setAccessible( true );

			return ( u ) -> {
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
			return null;
		}
	}

	private void add( Type type, Function<String, ?> func, int prio )
	{
		put( type, ( u ) -> func.apply( u ), prio );
	}

	private void addNullable( Type type, Function<String, ?> func, int prio )
	{
		put( type, nullable( ( u ) -> func.apply( u ) ), prio );
	}

	private void addPrimitive( Type type, Function<String, ?> func, int prio )
	{
		put( type, primitive( ( u ) -> func.apply( u ) ), prio );
	}

	private void add( ConfigConverter<?> c, int p )
	{
		final Class<? extends ConfigConverter> cls = c.getClass();
		final Type t = getTypeParameter( cls, CV_TYPE );

		if( t == null ) {
			throw new IllegalArgumentException( format( "No type information for converter %s", cls.getName() ) );
		}

		put( t, c, p );
	}

	private void put( Type type, ConfigConverter<?> conv, int priority )
	{
		this.cached.compute( type, ( t, h ) -> {
			if( h == null || h.p < priority ) {
				final ConfigConverter<?> c = conv.isNullHandled() ? conv : nullable( conv );

				return new CCH<>( c, priority );
			}
			else {
				return h;
			}
		} );
	}
}
