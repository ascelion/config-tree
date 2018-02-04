
package ascelion.config.utils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.eclipse.ext.ConfigExt.Value;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.text.StringEscapeUtils.unescapeJava;

import lombok.Getter;
import lombok.ToString;

@ToString( of = { "expression", "cached", "value" }, doNotUseGetters = true )
public final class Expression
{

	static private Function<String, Value> wrap( UnaryOperator<String> fun )
	{
		return x -> {
			x = fun.apply( x );

			if( x == null ) {
				return new Value();
			}
			else {
				return new Value( x );
			}
		};
	}

	static private final char[] PREFIX = "${".toCharArray();
	static private final char[] DEFAULT = ":-".toCharArray();
	static private final char[] SUFFIX = "}".toCharArray();
	static private final char ESCAPE = '\\';

	private final Function<String, Value> lookup;
	private final Deque<String> names = new LinkedList<>();
	@Getter
	private String expression;
	private String value;
	@Getter
	private String defValue;
	private boolean cached;
	@Getter
	private boolean changed;
	@Getter
	private String lastVariable;

	public Expression( UnaryOperator<String> lookup )
	{
		this( wrap( lookup ), null );
	}

	public Expression( UnaryOperator<String> lookup, String expression )
	{
		this( wrap( lookup ), expression );
	}

	public Expression( Function<String, Value> lookup )
	{
		this( lookup, null );
	}

	public Expression( Function<String, Value> lookup, String expression )
	{
		this.lookup = lookup;

		setExpression( expression );
	}

	public void setExpression( String expression )
	{
		this.expression = expression;
		this.cached = isBlank( expression );
		this.changed = false;
		this.value = null;
	}

	public boolean isEmpty()
	{
		return isBlank( this.expression );
	}

	public String getValue()
	{
		if( this.cached ) {
			return this.value;
		}
		if( this.cached ) {
			return this.value;
		}

		final Buffer buf = new Buffer( this.expression );

		this.value = unescapeJava( replace( buf ) );
		this.cached = true;

		return this.value;
	}

	public void expire()
	{
		this.cached = isBlank( this.expression );
		this.value = null;
	}

	private String replace( Buffer buf )
	{
		for( int o1 = buf.offset; o1 < buf.count; o1++ ) {
			if( buf.matches( PREFIX, o1, ESCAPE ) ) {
				final int start = o1 + PREFIX.length;
				int nested = 0;

				for( int o2 = start; o2 < buf.count; o2++ ) {
					if( buf.matches( PREFIX, o2, ESCAPE ) ) {
						nested++;

						continue;
					}

					if( buf.matches( SUFFIX, o2, ESCAPE ) ) {
						if( nested == 0 ) {
							final Buffer place = buf.newBuffer( start, o2 - start );

							replace( place );

							final int defIx = place.find( DEFAULT, ESCAPE );
							final String var;

							if( defIx < 0 ) {
								var = place.toString();
								this.defValue = null;
							}
							else {
								var = place.toString( 0, defIx );
								this.defValue = place.toString( defIx + DEFAULT.length, place.count - defIx - DEFAULT.length );
							}

							pushName( var );

							final Value res = this.lookup.apply( var );
							String val;
							if( res.undefined() ) {
								if( this.defValue == null ) {
									throw new ConfigNotFoundException( var );
								}

								val = this.defValue;
							}
							else {
								val = res.get();
							}

							val = replace( new Buffer( trimToEmpty( val ) ) );

							final int end = o2 + SUFFIX.length;
							final int dif = buf.replace( o1, end - o1, val );

							o1 = end + dif - 1;

							this.changed = true;

							popName();

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

	private void pushName( String name )
	{
		if( this.names.contains( name ) ) {
			final String m = format( "Recursive definition for ${%s}: %s", name, this.names.stream().collect( joining( " -> " ) ) );

			throw new IllegalStateException( m );
		}

		this.names.addLast( name );

		this.lastVariable = name;
	}

	private void popName()
	{
		this.names.pollLast();
	}
}
