
package ascelion.config.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigNode;

import static ascelion.config.impl.Utils.keys;
import static ascelion.config.impl.Utils.path;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

final class ConfigNodeImpl implements ConfigNode
{

	private final String name;
	private final String path;
	private String item;
	private Map<String, ConfigNodeImpl> tree;

	public ConfigNodeImpl()
	{
		this.name = null;
		this.path = null;
	}

	public ConfigNodeImpl( String name )
	{
		this.name = name;
		this.path = null;
	}

	public ConfigNodeImpl( String name, ConfigNodeImpl parent )
	{
		this.name = name;
		this.path = path( path( parent ), name );

		if( parent != null ) {
			parent.tree( true ).put( this.path, this );
		}
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
		return this.item;
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

		if( this.item != null ) {
			if( this.path != null ) {
				sb.append( "{" );
			}
			sb.append( this.item );
		}
		if( this.tree != null ) {
			if( sb.length() > 0 ) {
				sb.append( ", " );
			}
			else if( this.path != null ) {
				sb.append( "{" );
			}

			sb.append( this.tree.entrySet().stream().map( Object::toString ).collect( Collectors.joining( ", " ) ) );
		}
		if( this.path != null && sb.length() > 0 ) {
			sb.append( "}" );
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

			this.item = c.stream().map( Object::toString ).collect( Collectors.joining( "," ) );

			return;
		}
		if( payload instanceof Object[] ) {
			final Object[] v = (Object[]) payload;

			this.item = Stream.of( v ).map( Object::toString ).collect( Collectors.joining( "," ) );

			return;
		}

		this.item = Objects.toString( payload, null );
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

			m.put( p, f.apply( this.item ) );
		}
		else {
			this.tree.forEach( ( k, v ) -> v.fillMap( unwrap, m, f ) );
		}
	}
}
