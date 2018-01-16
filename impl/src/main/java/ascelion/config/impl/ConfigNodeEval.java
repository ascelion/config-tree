
package ascelion.config.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigNode;

import static ascelion.config.impl.Utils.keys;
import static ascelion.config.impl.Utils.path;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;

import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StrSubstitutor;

class ConfigNodeEval implements ConfigNode
{

	private final ConfigNode root;
	private final String name;
	private final String path;
	private final StrSubstitutor sub;
	private final PropertyChangeSupport pcs;
	private final Map<String, ConfigNodeEval> nodes = new TreeMap<>();
	private String value;

	ConfigNodeEval()
	{
		this.root = this;
		this.name = null;
		this.path = null;
		this.pcs = new PropertyChangeSupport( this );

		this.sub = new StrSubstitutor();
		this.sub.setEscapeChar( '\\' );
		this.sub.setValueDelimiter( ':' );
		this.sub.setEnableSubstitutionInVariables( true );

		final StrLookup<String> res = new StrLookup<String>()
		{

			@Override
			public String lookup( String key )
			{
				return getValue( key );
			}
		};

		this.sub.setVariableResolver( res );

	}

	private ConfigNodeEval( ConfigNodeEval parent, String name )
	{
		this.name = name;
		this.path = path( path( parent ), name );
		this.root = parent.root;
		this.pcs = parent.pcs;
		this.sub = parent.sub;

		parent.nodes.put( name, this );
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
	public Kind getKind()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getValue()
	{
		return (T) this.value;
	}

	@Override
	public Set<String> getKeys()
	{
		final Set<String> keys = new TreeSet<>();

		getKeys( this, keys );

		return unmodifiableSet( keys );
	}

	@Override
	public ConfigNode getNode( String path )
	{
		return findNode( path, false );
	}

	@Override
	public <T> T getValue( String path )
	{
		final ConfigNode node = findNode( path, false );

		return node != null ? node.getValue() : null;
	}

	@Override
	public Collection<ConfigNode> getNodes()
	{
		return unmodifiableCollection( this.nodes.values() );
	}

	@Override
	public void addChangeListener( PropertyChangeListener pcl )
	{
		this.pcs.addPropertyChangeListener( pcl );
	}

	@Override
	public void removeChangeListener( PropertyChangeListener pcl )
	{
		this.pcs.removePropertyChangeListener( pcl );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		if( this.value != null ) {
			sb.append( "item: " ).append( this.value );
		}
		if( this.nodes.size() > 0 ) {
			if( sb.length() > 0 ) {
				sb.append( ", " );
			}

			sb.append( "tree: " )
				.append( this.nodes.entrySet().stream().map( Objects::toString ).collect( joining( ", ", "{", "}" ) ) );
		}

		return sb.toString();
	}

	public void setValue( String path, Object value )
	{
		findNode( path, true ).setValue( value );
	}

	public void setValue( Object value )
	{
		if( value instanceof Map ) {
			final Map<String, Object> ms = (Map<String, Object>) value;

			ms.forEach( ( k, s ) -> {
				setValue( k, s );
			} );

			return;
		}
		if( value instanceof Collection ) {
			final Collection<?> c = (Collection<?>) value;

			setValue( c.stream().map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}
		if( value instanceof Object[] ) {
			final Object[] v = (Object[]) value;

			setValue( Stream.of( v ).map( Object::toString ).collect( Collectors.joining( "," ) ) );

			return;
		}

		final String newValue = Objects.toString( value, null );

		if( !Objects.equals( this.value, newValue ) ) {
			final String oldValue = this.value;

			this.value = newValue;

			this.pcs.firePropertyChange( this.path, oldValue, newValue );
		}
	}

	private void getKeys( ConfigNodeEval node, Set<String> keys )
	{
		if( node.value != null ) {
			keys.add( this.path );
		}

		node.nodes.values().forEach( c -> getKeys( c, keys ) );
	}

	private ConfigNodeEval findNode( String path, boolean create )
	{
		path = this.sub.replace( path );

		final String[] keys = keys( path );
		ConfigNodeEval node = this;

		for( final String key : keys ) {
			if( node == null ) {
				return null;
			}

			node = node.child( key, create );
		}

		return node;
	}

	private ConfigNodeEval child( String name, boolean create )
	{
		if( create ) {
			return this.nodes.computeIfAbsent( name, x -> new ConfigNodeEval( this, name ) );
		}
		else {
			return this.nodes.get( name );
		}
	}

}
