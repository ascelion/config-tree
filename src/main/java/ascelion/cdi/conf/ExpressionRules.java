
package ascelion.cdi.conf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ascelion.cdi.conf.ExpressionParser.RootContext;
import ascelion.shared.cdi.conf.ConfigNode;

import static ascelion.cdi.conf.ExpressionParser.RULE_expr;
import static ascelion.cdi.conf.ExpressionParser.RULE_root;
import static ascelion.cdi.conf.ExpressionParser.VOCABULARY;
import static java.lang.String.format;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExpressionRules
{

	static private final Logger L = LoggerFactory.getLogger( ExpressionRules.class );

	static abstract class Rule
	{

		final Rule parent;
		final List<Rule> children = new ArrayList<>();

		Rule( Rule parent )
		{
			this.parent = parent;
		}

		void addChild( Rule rule )
		{
			this.children.add( rule );
		}

		void addTerm( TerminalNode t )
		{
			if( t.getSymbol().getType() >= 0 ) {
				addChild( new Term( t, this ) );
			}
		}

		String evaluate( ConfigNode root, Set<String> properties )
		{
			return this.children.stream().map( rule -> rule.evaluate( root, properties ) ).collect( Collectors.joining() );
		}

		String getValue()
		{
			return this.children.stream().map( Rule::getValue ).collect( Collectors.joining() );
		}

		@Override
		public String toString()
		{
			return format( "%s: %s", getClass().getSimpleName(), getValue() );
		}

		String toJson()
		{
			return new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeHierarchyAdapter( Rule.class, new RuleTA<>() )
				.create()
				.toJson( this );
		}
	}

	static class Root extends Rule
	{

		Root()
		{
			super( null );
		}
	}

	static class Expr extends Rule
	{

		final List<Rule> val = new ArrayList<>();
		final List<Rule> def = new ArrayList<>();

		boolean right;

		Expr( Rule parent )
		{
			super( parent );
		}

		@Override
		void addChild( Rule rule )
		{
			super.addChild( rule );

			if( rule instanceof Expr || Term.isA( rule, ExpressionParser.ITEM ) ) {
				if( this.right ) {
					this.def.add( rule );
				}
				else {
					this.val.add( rule );
				}
			}
		}

		@Override
		void addTerm( TerminalNode t )
		{
			super.addTerm( t );

			if( t.getSymbol().getType() == ExpressionParser.DEF ) {
				this.right = true;
			}
		}

		@Override
		String evaluate( ConfigNode root, Set<String> properties )
		{
			final Eval d = new Eval( this.def, properties );
			String v = new Eval( this.val, properties ).get( root );

			if( v == null ) {
				v = d.get( root );
			}

			final Rule first = this.children.get( 0 );
			final boolean eval = Term.isA( first, ExpressionParser.BEG );

			if( eval ) {
				if( !properties.add( v ) ) {
					throw new ExpressionException( format( "Recursive definition: %s", getValue() ) );
				}
				v = root.getValue( v );

				if( v == null ) {
					v = d.get( root );
				}
			}

			if( v == null ) {
				return null;
			}

			if( v.contains( "${" ) ) {
				return parse( v ).evaluate( root, properties );
			}
			else {
				return v;
			}
		}
	}

	static class Term extends Rule
	{

		static boolean isA( Rule rule, int type )
		{
			if( Term.class.isInstance( rule ) ) {
				final Term item = (Term) rule;

				return item.isA( type );
			}

			return false;
		}

		final TerminalNode node;

		Term( TerminalNode node, Rule parent )
		{
			super( parent );

			this.node = node;
		}

		@Override
		String getValue()
		{
			return this.node != null ? this.node.getText() : null;
		}

		@Override
		public String toString()
		{
			if( this.node == null ) {
				return null;
			}

			return format( "%s: %s", VOCABULARY.getSymbolicName( this.node.getSymbol().getType() ), this.node.getText() );
		}

		@Override
		String evaluate( ConfigNode root, Set<String> properties )
		{
			return getValue();
		}

		boolean isA( int type )
		{
			return this.node != null ? this.node.getSymbol().getType() == type : false;
		}
	}

	static class Eval
	{

		private final List<Rule> children;
		private ConfigNode root;
		private String value;
		private final Set<String> properties;

		Eval( List<Rule> children, Set<String> properties )
		{
			this.children = children;
			this.properties = properties;
		}

		String get( ConfigNode root )
		{
			if( this.root != root ) {
				this.value = this.children.stream().map( rule -> rule.evaluate( root, this.properties ) ).collect( Collectors.joining() );

				this.root = root;
			}

			return this.value;
		}
	}

	static class RuleTA<T extends Rule> extends TypeAdapter<T>
	{

		@Override
		public void write( JsonWriter out, Rule rule ) throws IOException
		{
			out.beginObject();
			out.name( "type" );
			out.value( rule.getClass().getSimpleName() );
			out.name( "value" );
			out.value( rule.getValue() );
			out.name( "children" );
			out.beginArray();
			for( final Rule c : rule.children ) {
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

	static Rule parse( String value )
	{
		final ExpressionParser px = ExpressionParser.createFor( value, System.err::println );
		final RootContext cx = px.root();

		if( px.getNumberOfSyntaxErrors() > 0 ) {
			throw new ExpressionException( value, px.getErrors() );
		}

		final Rule rule = toRule( null, cx );

		L.trace( "{} -> {}", value, rule.toJson() );

		return rule;
	}

	static Rule toRule( Rule parent, ParseTree tree )
	{
		Rule rule = null;

		if( tree instanceof ParserRuleContext ) {
			final ParserRuleContext rc = (ParserRuleContext) tree;

			switch( rc.getRuleIndex() ) {
				case RULE_root: {
					rule = new Root();
				}
				break;

				case RULE_expr: {
					rule = new Expr( parent );
				}
				break;

			}

			for( final ParseTree t : rc.children ) {
				toRule( rule, t );
			}

			if( parent != null ) {
				parent.addChild( rule );
			}
		}
		else {
			parent.addTerm( (TerminalNode) tree );
		}

		return rule;
	}

}
