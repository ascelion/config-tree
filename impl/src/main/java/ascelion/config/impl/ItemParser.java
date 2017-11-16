
package ascelion.config.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import ascelion.config.impl.ItemTokenizer.Token;

import static java.lang.String.format;

final class ItemParser
{

	private final Deque<ItemTokenizer.Token> begTokens = new LinkedList<>();

	private final String content;

	private final List<EvalError> errors = new ArrayList<>();

	ItemParser( String content )
	{
		this.content = content;
	}

	@SuppressWarnings( "incomplete-switch" )
	<T> T parse( ItemParserListener<T> listener )
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
						this.errors.add( new EvalError( format( "unexpected token '%s'", tok.type.value ), tok.position ) );
					}
				break;

				case END:
					try {
						this.begTokens.pop();
					}
					catch( final NoSuchElementException e ) {
						this.errors.add( new EvalError( format( "unbalanced token '%s'", tok.type.value ), tok.position ) );
					}
				break;
			}

			listener.seen( tok );
		}

		this.errors.addAll( tkz.getErrors() );

		if( this.begTokens.size() > 0 ) {
			this.errors.add( new EvalError( "unbalanced '${'", this.begTokens.peek().position ) );
		}

		if( this.errors.size() > 0 ) {
			try {
				throw new EvalException( this.content, this.errors );
			}
			finally {
				this.errors.clear();
				this.begTokens.clear();
			}
		}

		return listener.finish();
	}

}
