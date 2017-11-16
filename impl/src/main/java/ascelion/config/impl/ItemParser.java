
package ascelion.config.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import ascelion.config.api.ConfigParseException;
import ascelion.config.api.ConfigParsePosition;
import ascelion.config.impl.ItemTokenizer.Token;

import static java.lang.String.format;

final class ItemParser
{

	interface Listener<T>
	{

		void start();

		void seen( Token tok );

		T finish();
	}

	private final Deque<ItemTokenizer.Token> begTokens = new LinkedList<>();

	private final String content;

	private final List<ConfigParsePosition> errors = new ArrayList<>();

	ItemParser( String content )
	{
		this.content = content;
	}

	@SuppressWarnings( "incomplete-switch" )
	<T> T parse( Listener<T> listener )
	{
		final StringReader rd = new StringReader( this.content );
		final ItemTokenizer tkz = new ItemTokenizer( rd );

		this.errors.clear();

		listener.start();

		Token tok;

		while( ( tok = tkz.next() ) != null ) {

			switch( tok.type ) {
				case BEG:
					this.begTokens.push( tok );
				break;

				case DEF:
					if( this.begTokens.isEmpty() ) {
						this.errors.add( new ConfigParsePosition( format( "unexpected token '%s'", tok.type.value ), tok.position ) );
					}
				break;

				case END:
					try {
						this.begTokens.pop();
					}
					catch( final NoSuchElementException e ) {
						this.errors.add( new ConfigParsePosition( format( "unbalanced token '%s'", tok.type.value ), tok.position ) );
					}
				break;
			}

			listener.seen( tok );
		}

		this.errors.addAll( tkz.getErrors() );

		if( this.begTokens.size() > 0 ) {
			this.errors.add( new ConfigParsePosition( "unbalanced '${'", this.begTokens.peek().position ) );
		}

		if( this.errors.size() > 0 ) {
			try {
				throw new ConfigParseException( this.content, this.errors );
			}
			finally {
				this.errors.clear();
				this.begTokens.clear();
			}
		}

		return listener.finish();
	}

}
