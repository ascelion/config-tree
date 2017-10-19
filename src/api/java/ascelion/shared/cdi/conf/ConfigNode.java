
package ascelion.shared.cdi.conf;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class ConfigNode
{

	static String[] keys( String path )
	{
		return path != null ? path.split( "\\." ) : new String[0];
	}

	static String path( int s, int e, String[] keys )
	{
		return asList( keys ).subList( s, e ).stream().collect( Collectors.joining( "." ) );
	}

	private ConfigNode parent;
	private final String name;
	private String item;
	private Map<String, ConfigNode> tree;

	public ConfigNode()
	{
		this( null, null );
	}

	public ConfigNode( String name )
	{
		this( name, null );
	}

	public ConfigNode( String name, ConfigNode parent )
	{
		this.name = name;

		parent( parent );
	}

	public ConfigNode parent( ConfigNode parent )
	{
		if( this.parent != null ) {
			this.parent.tree().remove( this.name );
		}

		this.parent = parent;

		if( this.parent != null ) {
			this.parent.tree().put( this.name, this );
		}

		return this;
	}

	public String getName()
	{
		return this.name;
	}

	public String getPath()
	{
		return ConfigItem.fullPath( this.parent != null ? this.parent.getPath() : null, this.name );
	}

	public String getItem()
	{
		return this.item;
	}

	public Map<String, ConfigNode> getTree()
	{
		return this.tree != null ? unmodifiableMap( this.tree ) : null;
	}

	public String getItem( String path )
	{
		return get( String.class, path );
	}

	public ConfigNode getNode( String path )
	{
		return get( ConfigNode.class, path );
	}

	public Map<String, ConfigNode> getTree( String path )
	{
		return get( Map.class, path );
	}

	private <T> T get( Class<T> type, String path )
	{
		if( type != String.class && type != Map.class ) {
		}

		final String[] keys = keys( path );
		ConfigNode node = this;

		for( final String k : keys ) {
			if( node == null || node.tree == null ) {
				return null;
			}

			node = node.tree().get( k );
		}

		if( node == null ) {
			return null;
		}

		if( type == ConfigNode.class ) {
			return (T) node;
		}
		if( type == String.class ) {
			return (T) node.getItem();
		}
		if( type == Map.class ) {
			return (T) node.getTree();
		}

		throw new IllegalArgumentException( format( "Cannot handle type %s", type.getName() ) );
	}

	public ConfigNode set( Object payload )
	{
		if( payload instanceof Map ) {
			final Map<String, Object> ms = (Map<String, Object>) payload;

			ms.forEach( ( k, s ) -> {
				set( k, s );
			} );

			return this;
		}
		if( payload instanceof ConfigNode ) {
			final ConfigNode o = (ConfigNode) payload;

			return set( o );
		}
		if( payload instanceof Collection ) {
			final Collection<?> c = (Collection<?>) payload;
			final String s = c.stream().map( Object::toString ).collect( Collectors.joining( "," ) );

			return set( s );
		}
		if( payload instanceof Object[] ) {
			final Object[] v = (Object[]) payload;
			final String s = Stream.of( v ).map( Object::toString ).collect( Collectors.joining( "," ) );

			return set( s );
		}

		return set( payload != null ? payload.toString() : null );
	}

	public ConfigNode set( String item )
	{
		this.item = item;

		return this;
	}

	public ConfigNode set( ConfigNode node )
	{
		node.parent( this );

		return this;
	}

	public void set( String path, Object payload )
	{
		if( isBlank( path ) ) {
			set( payload );
		}
		else {
			final String[] keys = keys( path );

			ConfigNode node = this;

			for( final String key : keys ) {
				final ConfigNode temp = node;

				node = node.tree().computeIfAbsent( key, k -> new ConfigNode( k, temp ) );
			}

			node.set( payload );
		}
	}

	Map<String, ConfigNode> tree()
	{
		if( this.tree == null ) {
			this.tree = new TreeMap<>();
		}

		return this.tree;
	}

	@Override
	public String toString()
	{
		return Stream.of( this.item, this.tree ).filter( Objects::nonNull )
			.map( Object::toString ).collect( Collectors.joining( ",", "{", "}" ) );
	}
}
