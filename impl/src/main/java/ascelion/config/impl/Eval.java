
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigParsePosition;

public class Eval
{

	enum Context
	{
		COLLECT,
		ESCAPE,
		DOLLAR,
		VALUE,
		DEFAULT,
	}

	private final Deque<Context> contexts = new LinkedList<>();
	private final char[] content;
	private final StringBuilder result = new StringBuilder();
	private List<ConfigParsePosition> errors = new ArrayList<>();
	private int offset;

	Eval( String content )
	{
		this.content = content.toCharArray();
		this.offset = 0;
	}

	Eval( Eval parent )
	{
		this.content = parent.content;
		this.offset = parent.offset;
		this.errors = parent.errors;
	}

	ConfigNode next( ConfigNode root )
	{
		push( Context.COLLECT );

		while( this.offset < this.content.length ) {
			final char c = this.content[this.offset++];

			switch( top() ) {
				case ESCAPE:
					this.result.append( c );

					pop();
				break;

				case COLLECT:
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '$':
							push( Context.DOLLAR );
						break;

						default:
							this.result.append( c );
					}
				break;

				case DOLLAR:
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '{':
							push( Context.VALUE );
						break;

						default:
							pop();
							this.result.append( c );
					}
				break;

				case VALUE:
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '}':
							pop();
						// TODO return token
//							return new Token( c, null, this.result );
						break;

						case ':':
							pop();
							push( Context.DEFAULT );
						// TODO return token
						break;

						default:
							this.result.append( c );
					}
				break;

				case DEFAULT: {
					switch( c ) {
						case '\\':
							push( Context.ESCAPE );
						break;

						case '}':
							pop();
						// TODO return token
						break;

						default:
							this.result.append( c );
					}
				}
			}
		}

		pop();

		return root;
	}

	private void pop()
	{
		this.contexts.pop();
	}

	private Context top()
	{
		return this.contexts.peek();
	}

	private void push( Context context )
	{
		this.contexts.push( context );
	}
}
