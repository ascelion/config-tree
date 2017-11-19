
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.impl.ItemTokenizer.Token;

import static ascelion.config.impl.Utils.keys;
import static ascelion.config.impl.Utils.path;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;;

final class ConfigNodeImpl implements ConfigNode
{

	static private final Pattern SHORTCUT = Pattern.compile( "^([^${}:]*):(.+)$" );

	static private class EvalResult
	{

		final String item;
		final ConfigNode node;

		EvalResult( String item )
		{
			this.item = item;
			this.node = null;
		}

		EvalResult( ConfigNode node )
		{
			this.item = node.getValue();
			this.node = node;
		}
	}

	static private class Item
	{

		Expr parent;

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

			final Item that = (Item) obj;

			return Objects.equals( toString(), that.toString() );
		}
	}

	static private class Type extends Item
	{

		final Token.Type type;

		Type( ascelion.config.impl.ItemTokenizer.Token.Type type )
		{
			this.type = type;
		}

		@Override
		public String toString()
		{
			return this.type.value;
		}
	}

	static private abstract class Eval extends Item
	{

		abstract EvalResult eval( ConfigNodeImpl root );

		abstract boolean isEvaluable();
	}

	static private class Text extends Eval
	{

		final String text;

		Text( String text )
		{
			this.text = text;
		}

		@Override
		public String toString()
		{
			return this.text;
		}

		@Override
		EvalResult eval( ConfigNodeImpl root )
		{
			return new EvalResult( this.text.replace( "\\:", ":" ) );
		}

		@Override
		boolean isEvaluable()
		{
			return false;
		}
	}

	static private class Expr extends Eval
	{

		static private final ThreadLocal<Deque<Expr>> CHAIN = new ThreadLocal<Deque<Expr>>()
		{

			@Override
			protected Deque<Expr> initialValue()
			{
				return new LinkedList<>();
			};
		};

		final List<Item> children = new ArrayList<>();
		final List<Eval> val = new ArrayList<>();
		final List<Eval> def = new ArrayList<>();
		List<Eval> add = this.val;

		@Override
		public String toString()
		{
			return this.children.stream().map( Object::toString ).collect( joining() );
		}

		<T extends Item> T addChild( T child )
		{
			child.parent = this;

			this.children.add( child );

			if( child instanceof Eval ) {
				this.add.add( (Eval) child );
			}

			return child;
		}

		@Override
		EvalResult eval( ConfigNodeImpl root )
		{
			final StringBuilder b = new StringBuilder();

			b.insert( 0, this );

			CHAIN.get().forEach( x -> {
				b.insert( 0, "->" );
				b.insert( 0, x );
				if( Objects.equals( this, x ) ) {
					throw new ConfigLoopException( format( "recursive definition: %s", b ) );
				}
			} );

			EvalResult result = eval( this.val, root );

			if( isEvaluable() ) {
				CHAIN.get().push( this );

				try {
					final ConfigNodeImpl node = root.findNode( result.item, false );

					if( node != null ) {
						result = new EvalResult( node );
					}
					else {
						result = null;
					}
				}
				finally {
					CHAIN.get().pop();
				}
			}

			if( result == null ) {
				result = eval( this.def, root );
			}

			if( result == null && isEvaluable() ) {
				throw new ConfigNotFoundException( toString() );
			}

			return result;
		}

		final EvalResult eval( List<Eval> elements, ConfigNodeImpl root )
		{
			switch( elements.size() ) {
				case 0:
					return null;

				case 1:
					return elements.get( 0 ).eval( root );

				default:
					return new EvalResult( elements.stream().map( rule -> rule.eval( root ).item ).collect( Collectors.joining() ) );
			}
		}

		boolean isExpression()
		{
			return this.children.stream().anyMatch( c -> {
				return Type.class.isInstance( c ) || Expr.class.isInstance( c );
			} );
		}

		@Override
		boolean isEvaluable()
		{
			return this.val.size() == 1 && this.children.size() > 2;
		}

		Expr seen( Token tok )
		{
			switch( tok.type ) {
				case BEG: {
					final Expr expr = addChild( new Expr() );

					expr.addChild( new Type( Token.Type.BEG ) );

					return expr;
				}

				case END:
					addChild( new Type( tok.type ) );

					return this.parent;

				case DEF:
					addChild( new Type( tok.type ) );

					this.add = this.def;
					;
				break;

				case STR: {
					addChild( new Text( tok.text ) );
				}
				break;
			}

			return this;
		}
	}

	static private class Listener implements ItemParser.Listener<Expr>
	{

		private Expr expr;

		@Override
		public void start()
		{
			this.expr = new Expr();
		}

		@Override
		public void seen( Token tok )
		{
			this.expr = this.expr.seen( tok );
		}

		@Override
		public Expr finish()
		{
			return this.expr;
		}
	}

	private final String name;
	private final String path;
	private final ConfigNodeImpl root;
	private Expr expr;
	private Map<String, ConfigNodeImpl> tree;

	public ConfigNodeImpl()
	{
		this.name = null;
		this.path = null;
		this.root = this;
	}

	private ConfigNodeImpl( ConfigNodeImpl root )
	{
		this.name = null;
		this.path = null;
		this.root = root;
	}

	private ConfigNodeImpl( String name, ConfigNodeImpl parent )
	{
		this.name = name;
		this.path = path( path( parent ), name );
		this.root = parent.root;

		parent.tree( true ).put( name, this );
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getPath()
	{
		return this.path;
	}

	@Override
	public String getValue()
	{
		if( this.expr == null ) {
			return null;
		}

		return this.expr.eval( this.root ).item;
	}

	@Override
	public String getValue( String path )
	{
		if( isBlank( path ) ) {
			throw new IllegalArgumentException( "Configuration path cannot be null or empty" );
		}

		// handle special case "<STR>:<STR>"
		if( SHORTCUT.matcher( path ).matches() ) {
			path = "${" + path + "}";
		}

		final ConfigNodeImpl node;
		final Expr expr = ItemParser.parse( path, Listener::new );

		if( expr.isExpression() ) {
			node = new ConfigNodeImpl( this.root );

			node.set( expr );
		}
		else {
			node = findNode( path, false );

			if( node == null ) {
				throw new ConfigNotFoundException( path );
			}
		}

		return node.getValue();
	}

	@Override
	public ConfigNode getNode( String path )
	{
		if( isBlank( path ) ) {
			throw new IllegalArgumentException( "Configuration path cannot be null or empty" );
		}

		// handle special case "<STR>:<STR>"
		if( SHORTCUT.matcher( path ).matches() ) {
			path = "${" + path + "}";
		}

		final Expr expr = ItemParser.parse( path, Listener::new );
		final ConfigNode node;

		if( expr.isExpression() ) {
			node = expr.eval( this.root ).node;
		}
		else {
			node = findNode( path, false );
		}

		if( node == null ) {
			throw new ConfigNotFoundException( path );
		}

		return node;
	}

	@Override
	public Map<String, String> asMap()
	{
		return asMap( 0 );
	}

	@Override
	public Collection<? extends ConfigNode> getNodes()
	{
		return tree( false ).values();
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		if( this.expr != null ) {
			if( this.path != null ) {
				sb.append( "[" );
			}
			sb.append( this.expr );
		}
		if( this.tree != null ) {
			if( sb.length() > 0 ) {
				sb.append( ", " );
			}
			else if( this.path != null ) {
				sb.append( "[" );
			}

			sb.append( this.tree.entrySet().stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) );
		}
		if( this.path != null && sb.length() > 0 ) {
			sb.append( "]" );
		}

		return sb.toString();
	}

	String getLiteral()
	{
		return Objects.toString( this.expr, null );
	}

	Map<String, ConfigNodeImpl> tree( boolean create )
	{
		if( this.tree == null && create ) {
			this.tree = new TreeMap<>();
		}

		return this.tree != null ? this.tree : emptyMap();
	}

	void set( String path, Object value )
	{
		final Expr expr = ItemParser.parse( path, Listener::new );

		if( expr != null && expr.isExpression() ) {
			throw new IllegalArgumentException( path );
		}

		findNode( path, true ).set( value );
	}

	void set( Object value )
	{
		if( value instanceof Map ) {
			final Map<String, Object> ms = (Map<String, Object>) value;

			ms.forEach( ( k, s ) -> {
				set( k, s );
			} );

			return;
		}
		if( value instanceof Collection ) {
			final Collection<?> c = (Collection<?>) value;

			set( c.stream().map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}
		if( value instanceof Object[] ) {
			final Object[] v = (Object[]) value;

			set( Stream.of( v ).map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}
		if( value instanceof Expr ) {
			this.expr = (Expr) value;

			return;
		}

		if( value != null ) {
			this.expr = ItemParser.parse( Objects.toString( value, null ), Listener::new );
		}
		else {
			this.expr = null;
		}
	}

	private Map<String, String> asMap( int unwrap )
	{
		final TreeMap<String, String> m = new TreeMap<>();

		fillMap( unwrap, m );

		return m;
	}

	private ConfigNodeImpl findNode( String path, boolean create )
	{
		final String[] keys = keys( path );
		ConfigNodeImpl node = this;

		for( final String key : keys ) {
			if( node == null ) {
				return null;
			}

			node = node.child( key, create );
		}

		return node;
	}

	private ConfigNodeImpl child( String name, boolean create )
	{
		if( create ) {
			return tree( true ).computeIfAbsent( name, ignored -> new ConfigNodeImpl( name, this ) );
		}
		else {
			return tree( false ).get( name );
		}
	}

	private void fillMap( int unwrap, Map<String, String> m )
	{
		if( this.tree == null || this.tree.isEmpty() ) {
			String p = getPath();

			if( isBlank( p ) ) {
				return;
			}

			int u = unwrap;

			while( u-- > 0 ) {
				final int x = p.indexOf( '.' );

				if( x < 0 ) {
					throw new IllegalStateException( format( "Cannot unwrap %s from %s", p, getPath() ) );
				}

				p = p.substring( x + 1 );
			}

			m.put( p, getValue() );
		}
		else {
			this.tree.forEach( ( k, v ) -> v.fillMap( unwrap, m ) );
		}
	}

	@Override
	public Set<String> getKeys()
	{
		final Set<String> set = new TreeSet<>();

		fillEvaluables( this, set );

		return set;
	}

	static void fillEvaluables( ConfigNodeImpl node, Set<String> set )
	{
		if( node.expr != null ) {
			if( node.path != null ) {
				set.add( node.path );
			}

			addKeys( set, node.expr.val );
			addKeys( set, node.expr.def );
		}

		node.tree( false ).forEach( ( k, v ) -> fillEvaluables( v, set ) );
	}

	static void addKeys( Set<String> set, List<Eval> evals )
	{
		evals.stream()
			.filter( Expr.class::isInstance )
			.map( Expr.class::cast )
			.filter( e -> e.isEvaluable() ).forEach( e -> set.add( e.val.get( 0 ).toString() ) );
	}

	void add( ConfigNodeImpl node )
	{
		this.expr = node.expr;

		node.tree( false )
			.forEach( ( k, v ) -> {
				child( k, true ).add( v );
			} );
	}
}
