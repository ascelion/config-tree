
package ascelion.config.impl;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.UnaryOperator;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

@ToString( of = { "expression", "cached", "value" } )
final class Expression
{

	static private final char[] PREFIX = "${".toCharArray();
	static private final char[] DEFAULT = ":-".toCharArray();
	static private final char[] SUFFIX = "}".toCharArray();
	static private final char ESCAPE = '\\';

	private final UnaryOperator<String> lookup;
	@Getter( AccessLevel.PACKAGE )
	private String expression;
	private volatile String value;
	private volatile boolean cached;
	private final ReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Set<String> names = new LinkedHashSet<>();

	Expression( String expression, UnaryOperator<String> lookup )
	{
		this.lookup = lookup;

		setValue( expression );
	}

	Expression( UnaryOperator<String> lookup )
	{
		this.lookup = lookup;
	}

	void setValue( String expression )
	{
		this.rwl.writeLock().lock();

		try {
			this.expression = expression;
			this.cached = expression == null;
			this.value = null;
		}
		finally {
			this.rwl.writeLock().unlock();
		}
	}

	String getValue()
	{
		this.rwl.readLock().lock();

		try {
			if( this.cached ) {
				return this.value;
			}
		}
		finally {
			this.rwl.readLock().unlock();
		}

		this.rwl.writeLock().lock();

		try {
			if( this.cached ) {
				return this.value;
			}

			final Buffer buf = new Buffer( this.expression );

			this.value = unescapeJava( replace( buf ) );
			this.cached = true;

			return this.value;
		}
		finally {
			this.rwl.writeLock().unlock();
		}
	}

	void expire()
	{
		this.rwl.writeLock().lock();

		try {
			this.cached = false;
			this.value = null;
		}
		finally {
			this.rwl.writeLock().unlock();
		}
	}

	private String replace( Buffer buf )
	{
		return replace( buf, buf.offset(), buf.count() );
	}

	private String replace( Buffer buf, int offset, int count )
	{
		for( int o1 = offset; o1 < count; o1++ ) {
			if( buf.matches( PREFIX, o1, count, ESCAPE, offset ) ) {
				final int start = o1 + PREFIX.length;
				int nested = 0;

				for( int o2 = start; o2 < count; o2++ ) {
					if( buf.matches( PREFIX, o2, count ) ) {
						nested++;

						continue;
					}

					if( buf.matches( SUFFIX, o2, count ) ) {
						if( nested == 0 ) {
							final Buffer place = buf.subBuffer( start, o2 - start );

							replace( place );

							final int defIx = place.find( DEFAULT, ESCAPE );
							final Buffer varName;
							final Buffer varValue;

							if( defIx < 0 ) {
								varName = place;
								varValue = null;
							}
							else {
								varName = place.subBuffer( 0, defIx );
								varValue = place.subBuffer( defIx + DEFAULT.length, place.count() - defIx - DEFAULT.length );
							}

							final String var = varName.toString();

							addName( var );

							String val = this.lookup.apply( var );

							if( val == null ) {
								if( varValue != null ) {
									val = varValue.toString();
								}
							}
							if( val != null ) {
								final Buffer valBuf = new Buffer( val );

								val = replace( valBuf );

								final int end = o2 + SUFFIX.length;
								final int dif = buf.replace( o1, end - o1, val );

								o1 = end + dif - 1;
								count += dif;
							}

							delName( var );

							break;
						}
						else {
							nested--;
						}
					}
				}
			}
		}

		return buf.toString();
	}

	private void addName( String name )
	{
		if( !this.names.add( name ) ) {
			final String m = format( "Recursive definition for ${%s}: %s", name, this.names.stream().collect( joining( " -> " ) ) );

			throw new ConfigLoopException( m );
		}
	}

	private void delName( String name )
	{
		this.names.remove( name );
	}
}
