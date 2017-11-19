
package ascelion.config.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import ascelion.config.api.ConfigParseException;
import ascelion.config.api.ConfigParsePosition;
import ascelion.config.impl.ItemTokenizer.Token;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.trimToNull;

final class ItemParser
{

	static <T> T parse( String content, Supplier<Listener<T>> sup )
	{
		content = trimToNull( content );

		if( content == null ) {
			return null;
		}

		return new ItemParser( content ).parse( sup.get() );
	}

	interface Listener<T>
	{

		void start();

		void seen( Token tok );

		T finish();
	}

	private final String content;

	private ItemParser( String content )
	{
		this.content = content;
	}

	@SuppressWarnings( "incomplete-switch" )
	private <T> T parse( Listener<T> listener )
	{
		final StringReader rd = new StringReader( this.content );
		final ItemTokenizer tkz = new ItemTokenizer( rd );
		final Deque<ItemTokenizer.Token> begTokens = new LinkedList<>();
		final List<ConfigParsePosition> errors = new ArrayList<>();

		listener.start();

		Token tok;

		while( ( tok = tkz.next() ) != null ) {
			switch( tok.type ) {
				case BEG:
					begTokens.push( tok );
				break;

				case DEF:
					if( begTokens.isEmpty() ) {
						errors.add( new ConfigParsePosition( format( "unexpected token '%s'", tok.type.value ), tok.position ) );
					}
				break;

				case END:
					try {
						begTokens.pop();
					}
					catch( final NoSuchElementException e ) {
						errors.add( new ConfigParsePosition( format( "unbalanced token '%s'", tok.type.value ), tok.position ) );
					}
				break;
			}

			listener.seen( tok );
		}

		errors.addAll( tkz.getErrors() );

		if( begTokens.size() > 0 ) {
			errors.add( new ConfigParsePosition( "unbalanced '${'", begTokens.peek().position ) );
		}

		if( errors.size() > 0 ) {
			throw new ConfigParseException( this.content, errors );
		}

		return listener.finish();
	}

}
