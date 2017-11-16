
package ascelion.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.impl.ItemTokenizer.Token;

import static ascelion.config.impl.Utils.keys;
import static ascelion.config.impl.Utils.path;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;;

final class ConfigNodeImpl implements ConfigNode
{

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

		abstract String eval( ConfigNode root );

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
		String eval( ConfigNode root )
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
		String eval( ConfigNode root )
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

			String value = eval( this.val, root );

			if( follow() ) {
				CHAIN.get().push( this );

				try {
					value = root.getValue( value );
				}
				finally {
					CHAIN.get().pop();
				}
			}

			if( value == null ) {
				value = eval( this.def, root );
			}

			return value;
		}

		final String eval( List<Eval> elements, ConfigNode root )
		{
			if( elements.isEmpty() ) {
				return null;
			}

			return elements.stream().map( rule -> rule.eval( root ) ).collect( Collectors.joining() );
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

	static class Listener extends ItemParserListener<Eval>
	{

		private Expr expr;

		@Override
		void start()
		{
			this.expr = new Expr();
		}

		@Override
		void seen( Token tok )
		{
			this.expr = this.expr.seen( tok );
		}

		@Override
		Eval finish()
		{
			return this.expr;
		}
	}

	private final String name;
	private final String path;
	private final ConfigNode root;

	private Map<String, ConfigNodeImpl> tree;
	private Eval expr;

	public ConfigNodeImpl()
	{
		this.name = null;
		this.path = null;
		this.root = this;
	}

	private ConfigNodeImpl( String name, ConfigNodeImpl parent )
	{
		this.name = name;
		this.path = path( path( parent ), name );
		this.root = parent.root;

		parent.tree( true ).put( this.path, this );
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
	public ConfigNodeImpl getNode( String path )
	{
		return findNode( path, false );
	}

	@Override
	public Collection<? extends ConfigNode> getNodes()
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
	public <T> Map<String, T> asMap( int unwrap, Function<String, T> fun )
	{
		final TreeMap<String, T> m = new TreeMap<>();

		fillMap( unwrap, m, fun );

		return m;
	}

	void set( String path, Object payload )
	{
		findNode( path, true ).set( payload );
	}

	private void set( Object payload )
	{
		if( payload instanceof Map ) {
			final Map<String, Object> ms = (Map<String, Object>) payload;

			ms.forEach( ( k, s ) -> {
				set( k, s );
			} );

			return;
		}
		if( payload instanceof Collection ) {
			final Collection<?> c = (Collection<?>) payload;

			set( c.stream().map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}
		if( payload instanceof Object[] ) {
			final Object[] v = (Object[]) payload;

			set( Stream.of( v ).map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}

		if( payload != null ) {
			this.expr = new ItemParser( payload.toString() ).parse( new Listener() );
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
		final String path = path( this.path, name );

		if( create ) {
			return tree( true ).computeIfAbsent( path, ignored -> new ConfigNodeImpl( name, this ) );
		}
		else {
			return tree( false ).get( path );
		}
	}

	private Map<String, ConfigNodeImpl> tree( boolean create )
	{
		if( this.tree == null && create ) {
			this.tree = new TreeMap<>();
		}

		return this.tree != null ? this.tree : emptyMap();
	}

	private <T> void fillMap( int unwrap, TreeMap<String, T> m, Function<String, T> f )
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

			m.put( p, f.apply( getValue() ) );
		}
		else {
			this.tree.forEach( ( k, v ) -> v.fillMap( unwrap, m, f ) );
		}
	}
}
