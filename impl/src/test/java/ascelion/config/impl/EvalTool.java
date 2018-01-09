
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigParseException;
import ascelion.config.api.ConfigParsePosition;
import ascelion.config.impl.ItemTokenizer.Token;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public final class EvalTool
{

	static abstract class Rule
	{

		final List<Rule> children = new ArrayList<>();
		Rule parent;
		Rule base;

		<T extends Rule> T push( T child )
		{
			child.parent = child.base = this;

			this.children.add( child );

			return child;
		}

		<T extends Rule> T pop()
		{
			return (T) this.parent;
		}

		abstract String eval( ConfigNode root, Function<String, Rule> parser );

		@Override
		public String toString()
		{
			return this.children.stream().map( Rule::toString ).collect( Collectors.joining() );
		}

		@Override
		public boolean equals( Object obj )
		{
			if( obj == this ) {
				return true;
			}
			if( obj == null ) {
				return false;
			}

			if( getClass() != obj.getClass() ) {
				return false;
			}

			final Rule that = (Rule) obj;

			return Objects.equals( this.children, that.children );
		}
	}

	static class Text extends Rule
	{

		private final String text;

		Text( String text )
		{
			this.text = text;
		}

		@Override
		String eval( ConfigNode root, Function<String, Rule> parser )
		{
			return this.text;
		}

		@Override
		public String toString()
		{
			return this.text;
		}

		@Override
		public boolean equals( Object obj )
		{
			if( obj == this ) {
				return true;
			}
			if( obj == null ) {
				return false;
			}

			if( getClass() != obj.getClass() ) {
				return false;
			}

			final Text that = (Text) obj;

			return Objects.equals( this.text, that.text );
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode( this.text );
		}
	}

	static class Expr extends Rule
	{

		final List<Rule> val = new ArrayList<>();
		final List<Rule> def = new ArrayList<>();
		boolean right;

		void toDefault()
		{
			this.right = true;
		}

		@Override
		<T extends Rule> T push( T child )
		{
			super.push( child );

			if( this.right ) {
				this.def.add( child );
			}
			else {
				this.val.add( child );
			}

			return child;
		}

		@Override
		String eval( ConfigNode root, Function<String, Rule> parser )
		{
			final StringBuilder b = new StringBuilder();
			b.insert( 0, this );

			for( Rule ex = this.base; ex != null; ex = ex.base ) {
				b.insert( 0, "->" );
				b.insert( 0, ex );

				if( Objects.equals( this, ex ) ) {
					throw new ConfigException( format( "recursive definition: %s", b ) );
				}
			}

			final String path = eval( this.val, root, parser );

			if( path == null ) {
				throw new ConfigNotFoundException( toString() );
			}

			String item = getValue( root, path );

			if( item == null ) {
				item = eval( this.def, root, parser );
			}
			if( item == null ) {
				item = System.getProperty( path );
			}
			if( item == null ) {
				throw new ConfigNotFoundException( toString() );
			}

			if( item.contains( "${" ) ) {
				final Rule expr = parser.apply( item );

				expr.base = this;

				return expr.eval( root, parser );
			}
			else {
				return item;
			}
		}

		String getValue( ConfigNode root, final String prop )
		{
			if( this.parent != null || this.parent == null && this.val.stream().allMatch( Text.class::isInstance ) ) {
				return root.getNode( prop ).getValue();
			}

			return prop;
		}

		final String eval( List<Rule> rules, ConfigNode root, Function<String, Rule> parser )
		{
			switch( rules.size() ) {
				case 0:
					return null;
				case 1:
					return rules.get( 0 ).eval( root, parser );

				default:
					return rules.stream().map( rule -> rule.eval( root, parser ) ).collect( Collectors.joining() );
			}
		}

		@Override
		public String toString()
		{
			final StringBuilder b = new StringBuilder();

			this.val.forEach( b::append );

			if( this.def.size() > 0 ) {
				b.append( Token.S_DEF );

				this.def.forEach( b::append );
			}

			if( this.parent != null || this.parent == null && this.val.stream().allMatch( Text.class::isInstance ) ) {
				return format( "%s%s%s", Token.S_BEG, b, Token.S_END );
			}
			else {
				return b.toString();
			}
		}
	}

	static class Listener implements ContentParser.Listener<Expr>
	{

		Expr root;

		@Override
		public void start()
		{
			this.root = new Expr();
		}

		@SuppressWarnings( "incomplete-switch" )
		@Override
		public void seen( ItemTokenizer.Token tok )
		{
			if( this.root == null ) {
				throw new ConfigParseException( "unknown error", asList( new ConfigParsePosition( "unbalanced '}'", tok.position ) ) );
			}

			switch( tok.type ) {
				case BEG:
					this.root = this.root.push( new Expr() );
				break;

				case END:
					this.root = this.root.pop();
				break;

				case DEF:
					this.root.toDefault();
				break;

				case STR:
					this.root.push( new Text( tok.text ) );
				break;

			}
		}

		@Override
		public Expr finish()
		{
			if( this.root != null && this.root.val.stream().allMatch( Text.class::isInstance ) ) {
				final Expr expr = new Expr();

				expr.push( this.root );

				this.root = expr;
			}

			return this.root;
		}
	}

	static Expr parse( String content )
	{
		return ContentParser.parse( content, Listener::new );
	}

	public static String eval( String value, ConfigNode root )
	{
		final Rule rule = parse( value );

		return rule.eval( root, EvalTool::parse );
	}

}
