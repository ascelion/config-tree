
package ascelion.cdi.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static java.lang.String.format;

import org.junit.Test;

public class ValueParser
{

	static class Base
	{

		Base parent;
		final StringBuilder content = new StringBuilder();

		void addChild( Base base )
		{
			base.parent = this;
		}

		public void addChar( char c )
		{
			this.content.append( c );
		}

		@Override
		public String toString()
		{
			return format( "%s: %s", getClass().getSimpleName(), this.content.toString() );
		}
	}

	static class Root extends Base
	{
	}

	static class Expr extends Base
	{

		final List<? super Base> val = new ArrayList<>();
		final List<? super Base> def = new ArrayList<>();

		boolean right;

		@Override
		void addChild( Base base )
		{
			super.addChild( base );

			if( this.right ) {
				this.def.add( base );
			}
			else {
				this.val.add( base );
			}
		}

		@Override
		public void addChar( char c )
		{
			super.addChar( c );

			if( c == ':' ) {
				this.right = true;
			}
		}
	}

	static class Item extends Base
	{

	}

	void parse( String value )
	{
		final Stack<Base> stack = new Stack<>();
		stack.push( new Root() );

		final char[] s = value.toCharArray();

		for( int k = 0; k < s.length; k++ ) {
			k = parse( stack, s, k );
		}
	}

	static int parse( final Stack<Base> s, final char[] v, int k )
	{
		final char c = v[k];

		switch( c ) {
			case '$': {
				k++;

				if( k == v.length - 1 ) {
					throw new RuntimeException();
				}
				if( v[k] != '{' ) {
					throw new RuntimeException();
				}

				final Expr e = new Expr();

				s.peek().addChild( e );

				s.push( e );
			}
			break;

			case '}':
				s.pop();
			break;

			default:
				s.peek().addChar( c );
		}

		return k;
	}

	@Test
	public void run()
	{
		final ValueParser p = new ValueParser();

		p.parse( "${x-${a:b${c:d}}-y}" );
	}
}
