
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.impl.ItemTokenizer.Token;

import static ascelion.config.impl.Utils.keys;
import static ascelion.config.impl.Utils.path;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;;

final class ConfigNodeImpl implements ConfigNode
{

	static private final Pattern PATH_EXPRESSION = Pattern.compile( ".*[\\$\\{\\}:].*" );

	static class Item
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

	static class Type extends Item
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

	static abstract class Eval extends Item
	{

		abstract String eval( ConfigNodeImpl root );

		abstract boolean follow();
	}

	static class Text extends Eval
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
		String eval( ConfigNodeImpl root )
		{
			return this.text;
		}

		@Override
		boolean follow()
		{
			return false;
		}
	}

	static class Expr extends Eval
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
		Expr chain;

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
		String eval( ConfigNodeImpl root )
		{
			final StringBuilder b = new StringBuilder();

			b.insert( 0, this );

			CHAIN.get().forEach( x -> {
				b.insert( 0, "->" );
				b.insert( 0, x );
				if( Objects.equals( this, x ) ) {
					throw new ConfigException( format( "recursive definition: %s", b ) );
				}
			} );

			final String path = eval( this.val, root );
			String item = null;

			if( path != null && follow() ) {
				CHAIN.get().push( this );

				try {
					item = root.value( path );
				}
				catch( final ConfigNotFoundException e ) {
					item = null;
				}
				finally {
					CHAIN.get().pop();
				}
			}
			else {
				item = path;
			}

			if( item == null ) {
				item = eval( this.def, root );
			}

			return item;
		}

		final String eval( List<Eval> elements, ConfigNodeImpl root )
		{
			switch( elements.size() ) {
				case 0:
					return null;

				case 1:
					return elements.get( 0 ).eval( root );

				default:
					return elements.stream().map( rule -> rule.eval( root ) ).collect( Collectors.joining() );
			}
		}

		boolean isExpression()
		{
			return this.children.stream().anyMatch( Type.class::isInstance );
		}

		@Override
		boolean follow()
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

	static class Listener implements ItemParser.Listener<Expr>
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

	private Map<String, ConfigNodeImpl> tree;
	private Expr expr;

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
		return this.expr != null ? this.expr.eval( this.root ) : null;
	}

	@Override
	public ConfigNode getNode( String path )
	{
		if( isBlank( path ) ) {
			throw new IllegalArgumentException( "Configuration path cannot be null or empty" );
		}

		if( isNotBlank( path ) ) {
			if( PATH_EXPRESSION.matcher( path ).matches() ) {
				final ConfigNodeImpl node = new ConfigNodeImpl( this.root );

				node.set( path );

				return node;
			}
		}

		final ConfigNode node = findNode( path, false );

		if( node == null ) {
			throw new ConfigNotFoundException( path );
		}

		return node;
	}

	private String value( String path )
	{
		final ConfigNodeImpl node = node( path );

		return node != null ? node.getValue() : null;
	}

	private ConfigNodeImpl node( String path )
	{
		return findNode( path, false );
	}

	@Override
	public Collection<? extends ConfigNode> getChildren()
	{
		return this.tree != null ? this.tree.values() : emptyList();
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

	@Override
	public Map<String, String> asMap()
	{
		return asMap( 0 );
	}

	Map<String, String> asMap( int unwrap )
	{
		final TreeMap<String, String> m = new TreeMap<>();

		fillMap( unwrap, m );

		return m;
	}

	void setValue( Object value )
	{
		setValue( null, value );
	}

	void setValue( String path, Object value )
	{
		if( isNotBlank( path ) && PATH_EXPRESSION.matcher( path ).matches() ) {
			throw new IllegalArgumentException( path );
		}

		set( path, value );
	}

	private void set( String path, Object value )
	{
		findNode( path, true ).set( value );
	}

	private void set( Object value )
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

		if( value != null ) {
			this.expr = new ItemParser( value.toString() ).parse( new Listener() );
		}
		else {
			this.expr = null;
		}
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

	private Map<String, ConfigNodeImpl> tree( boolean create )
	{
		if( this.tree == null && create ) {
			this.tree = new TreeMap<>();
		}

		return this.tree != null ? this.tree : emptyMap();
	}

	private void fillMap( int unwrap, Map<String, String> m )
	{
		if( this.tree == null || this.tree.isEmpty() ) {
			String p = getPath();

			if( p.isEmpty() ) {
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
}
