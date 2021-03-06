
package ascelion.config.eval;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import ascelion.config.eval.Expression.Lookup;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
final class Replacer
{

	static private final ThreadLocal<Deque<Buffer>> DUMP = new ThreadLocal<Deque<Buffer>>()
	{

		@Override
		protected Deque<Buffer> initialValue()
		{
			return new LinkedList<>();
		};
	};

	private final Expression exp;
	private final Deque<Buffer> vars = new LinkedList<>();

	@Getter
	private String lastVariable;

	Buffer replace( String expression )
	{
		this.lastVariable = expression;

		return replace( new Buffer( expression ) );
	}

	private Buffer replace( Buffer buf )
	{
		DUMP.get().addLast( buf );

		try {
			return doReplace( buf );
		}
		finally {
			final Deque<Buffer> stack = DUMP.get();

			stack.pollLast();

			if( stack.isEmpty() ) {
				DUMP.remove();
			}
		}
	}

	private Buffer doReplace( Buffer buf )
	{
		int ofs1 = buf.offset;

		while( ofs1 < buf.offset + buf.count ) {
			int next = buf.match( this.exp.varPrefix, ofs1 );

			if( next > ofs1 ) {
				ofs1 = next;

				continue;
			}

			// prefix matched
			int ofs2 = ofs1 + this.exp.varPrefix.length;
			int open = 1;

			while( ofs2 < buf.offset + buf.count ) {
				next = buf.match( this.exp.varPrefix, ofs2 );

				if( next == ofs2 ) {
					// matched nested prefix
					open++;

					ofs2 += this.exp.varPrefix.length;

					continue;
				}

				next = buf.match( this.exp.varSuffix, ofs2 );

				if( next > ofs2 ) {
					ofs2 = next;

					continue;
				}
				// matched suffix
				if( --open > 0 ) {
					ofs2 += this.exp.varSuffix.length;

					continue;
				}

				handleLastSuffix( buf, ofs1, ofs2 - ofs1 );

				break;
			}

			if( open > 0 ) {
				break;
			}
		}

		return buf;
	}

	private void pushName( Buffer var )
	{
		if( this.vars.contains( var ) ) {
			final String m = format( "Recursive definition for ${%s}: %s", var,
				this.vars.stream().map( Buffer::toString ).collect( joining( " -> " ) ) );

			throw new IllegalStateException( m );
		}

		this.lastVariable = var.toString();

		this.vars.addLast( var );
	}

	private void popName()
	{
		this.vars.pollLast();
	}

	private void handleLastSuffix( final Buffer buf, final int ofs, final int cnt )
	{
		final Buffer place = buf.newBuffer( ofs + this.exp.varPrefix.length, cnt - this.exp.varPrefix.length );

		replace( place );

		final int defIx = place.find( this.exp.valueSep, 0 );
		final String def;
		Buffer var;

		if( defIx < 0 ) {
			var = place;
			def = "";
		}
		else {
			var = place.subBuffer( 0, defIx );
			def = place.subBuffer( defIx + this.exp.valueSep.length, place.count - defIx - this.exp.valueSep.length ).toString();
		}

		final Lookup res = this.exp.lookup.apply( var.toString() );
		String val = res.getValue( def );

		if( val == null ) {
			final String text = format( "Reference to undefined variable %s%s%s", new String( this.exp.varPrefix ), var, new String( this.exp.varSuffix ) );

			if( log.isErrorEnabled() ) {
				final String dump = DUMP.get().stream()
					.map( Buffer::toString )
					.collect( joining( "\n\t -> ", text + "\n\t -> ", "\n" ) );

				log.error( dump );
			}

			throw new NoSuchElementException( text );
		}

		pushName( var );

		try {
			val = replace( new Buffer( val ) ).toString();
		}
		finally {
			popName();
		}

		buf.replace( ofs, cnt + this.exp.varSuffix.length, val );
	}

}
