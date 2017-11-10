
package ascelion.cdi.conf;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

final class ConfigNodeImpl implements ConfigNode
{

	private ConfigNodeImpl parent;
	private final String name;
	private String item;
	private Map<String, ConfigNodeImpl> tree;

	public ConfigNodeImpl()
	{
		this( null, null );
	}

	public ConfigNodeImpl( String name )
	{
		this( name, null );
	}

	public ConfigNodeImpl( String name, ConfigNodeImpl parent )
	{
		this.name = name;

		parent( parent );
	}

	public ConfigNode parent( ConfigNodeImpl parent )
	{
		if( this.parent != null && parent.tree != null ) {
			this.parent.tree.remove( this.name );
		}

		this.parent = parent;

		if( this.parent != null ) {
			this.parent.tree( true ).put( this.name, this );
		}

		return this;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public String getPath()
	{
		return Utils.path( this.parent != null ? this.parent.getPath() : null, this.name );
	}

	@Override
	public Collection<? extends ConfigNode> getNodes()
	{
		return this.tree != null ? this.tree.values() : emptyList();
	}

	@Override
	public String getValue()
	{
		return this.item;
	}

	@Override
	public void setValue( String value )
	{
		this.item = value;
	}

	@Override
	public String getValue( String path )
	{
		final ConfigNodeImpl node = findNode( path, false );

		return node != null ? node.item : null;
	}

	@Override
	public void setValue( String path, String value )
	{
		findNode( path, true ).item = value;
	}

	@Override
	public void setValues( String path, Map<String, ?> values )
	{
		set( path, values );
	}

	@Override
	public void setValues( Map<String, ?> values )
	{
		set( null, values );
	}

	@Override
	public ConfigNodeImpl getNode( String path )
	{
		return findNode( path, false );
	}

	@Override
	public String toString()
	{
		return Stream.of( this.item, this.tree ).filter( Objects::nonNull )
			.map( Object::toString ).collect( Collectors.joining( ",", "{", "}" ) );
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

	void set( Object payload )
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
		final String[] keys = Utils.keys( path );
		ConfigNodeImpl node = this;

		for( final String key : keys ) {
			if( node == null || node.tree( create ) == null ) {
				return null;
			}

			final ConfigNodeImpl temp = node;

			if( create ) {
				node = node.tree( false ).computeIfAbsent( key, k -> new ConfigNodeImpl( k, temp ) );
			}
			else {
				node = node.tree( false ).get( key );
			}
		}

		return node;
	}

	Map<String, ConfigNodeImpl> tree( boolean create )
	{
		if( this.tree == null && create ) {
			this.tree = new TreeMap<>();
		}

		return this.tree != null ? this.tree : null;
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
