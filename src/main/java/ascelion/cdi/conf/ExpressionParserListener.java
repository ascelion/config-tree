
package ascelion.cdi.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ascelion.cdi.conf.ExpressionParser.RULE_expr;
import static ascelion.cdi.conf.ExpressionParser.RULE_path;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ExpressionParserListener extends ExpressionBaseListener
{

	public static class RuleTA<T extends Rule> extends TypeAdapter<T>
	{

		@Override
		public void write( JsonWriter out, Rule value ) throws IOException
		{
			out.beginObject();
			out.name( "type" );
			out.value( value.getClass().getSimpleName() );
			out.name( "terminals" );
			out.value( value.concatTerminals() );
			out.name( "children" );
			out.beginArray();
			for( final Rule c : value.children ) {
				write( out, c );
			}
			out.endArray();
			out.endObject();
		}

		@Override
		public T read( JsonReader in ) throws IOException
		{
			throw new UnsupportedOperationException();
		}

	}

	static abstract class Rule
	{

		private final Rule parent;
		private final List<Rule> children = new ArrayList<>();
		private final List<TerminalNode> terminals = new ArrayList<>();

		Rule( Rule parent )
		{
			this.parent = parent;

			if( parent != null ) {
				parent.children.add( this );
			}
		}

		final void concatTerminals( StringBuilder sb )
		{
			this.terminals.forEach( node -> sb.append( node.getText() ) );
		}

		final String concatTerminals()
		{
			final StringBuilder b = new StringBuilder();

			concatTerminals( b );

			return b.toString();
		}

		@Override
		public String toString()
		{
			final StringBuilder b = new StringBuilder();

			b.append( "{" );
			b.append( "type: " );
			b.append( getClass().getSimpleName() );
			b.append( ",terminals: [" );
			b.append( concatTerminals() );
			b.append( "]" );
			b.append( "}" );

			return b.toString();
		}
	}

	static class ExprRule extends Rule
	{

		ExprRule( Rule parent )
		{
			super( parent );
		}

	}

	static class PathRule extends Rule
	{

		PathRule( Rule parent )
		{
			super( parent );
		}

	}

	static class RootRule extends Rule
	{

		RootRule()
		{
			super( null );
		}
	}

	static private final Logger L = LoggerFactory.getLogger( ExpressionParserListener.class );

	private Rule rule = new RootRule();

	@Override
	public void enterEveryRule( ParserRuleContext ctx )
	{
		switch( ctx.getRuleIndex() ) {
			case RULE_expr:
				this.rule = new ExprRule( this.rule );
			break;

			case RULE_path:
				this.rule = new PathRule( this.rule );
			break;
		}
	}

	@Override
	public void exitEveryRule( ParserRuleContext ctx )
	{
		final String s = new GsonBuilder()
			.setPrettyPrinting()
			.registerTypeHierarchyAdapter( Rule.class, new RuleTA<>() )
			.create()
			.toJson( this.rule );

		L.info( "{}", s );

		this.rule = this.rule.parent;
	}

	@Override
	public void visitTerminal( TerminalNode node )
	{
		this.rule.terminals.add( node );
	}

}
