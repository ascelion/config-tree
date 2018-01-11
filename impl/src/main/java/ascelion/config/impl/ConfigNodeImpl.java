
package ascelion.config.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigException;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;

import static ascelion.config.impl.Utils.keys;
import static ascelion.config.impl.Utils.path;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

final class ConfigNodeImpl implements ConfigNode
{

	static class ConfigNodeTA extends TypeAdapter<ConfigNodeImpl>
	{

		@Override
		public void write( JsonWriter out, ConfigNodeImpl node ) throws IOException
		{
			switch( node.item.kindNoEval() ) {
				case NULL:
					out.nullValue();
				break;

				case ITEM:
				case LINK:
					out.value( node.getExpression() );
				break;

				case NODE:
					out.beginObject();
					for( final ConfigNodeImpl child : node.tree( false ).values() ) {
						out.name( child.getName() );
						write( out, child );
					}
					out.endObject();
			}
		}

		@Override
		public ConfigNodeImpl read( JsonReader in ) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	static private void fillKeys( ConfigNodeImpl node, Set<String> set )
	{
		switch( node.item.kindNoEval() ) {
			case NULL:
				if( node.path != null ) {
					set.add( node.path );
				}
			break;

			case ITEM:
				if( node.path != null ) {
					set.add( node.path );
				}

			case LINK:
				set.addAll( ( (ExpressionOLD) node.item.value() ).evaluables() );
			break;

			case NODE:
				node.tree( false ).forEach( ( k, v ) -> fillKeys( v, set ) );
			break;
		}
	}

	static private final Pattern SHORTCUT = Pattern.compile( "^([^${}:]*):(.+)$" );

	private final PropertyChangeSupport pcs = new PropertyChangeSupport( this );

	final String path;
	final ConfigNodeImpl root;
	final String name;
	private CachedItem item = new CachedItem( this );

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
	public ConfigNode.Kind getKind()
	{
		return this.item.kind();
	}

	@Override
	public <T> T getValue()
	{
		switch( this.item.kind() ) {
			case LINK:
			case NODE:
				return (T) ( (Map) this.item.cached() ).values();

			default:
				return this.item.cached();
		}
	}

	@Override
	public <T> T getValue( String path )
	{
		if( isBlank( path ) ) {
			throw new IllegalArgumentException( "Configuration path cannot be null or empty" );
		}

		// handle special case "<STR>:<STR>"
		if( SHORTCUT.matcher( path ).matches() ) {
			path = "${" + path + "}";
		}

		final ExpressionOLD expr = ExpressionOLD.compile( path );
		final ConfigNode node;

		if( expr.isExpression() ) {
			final CachedItem val = expr.eval( this );

			if( val.kind() != Kind.NODE ) {
				return (T) val.cached();
			}

			node = (ConfigNode) val.cached();
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

		final ExpressionOLD expr = ExpressionOLD.compile( path );
		final ConfigNode node;

		if( expr.isExpression() ) {
			final CachedItem val = expr.eval( this );

			node = val.node();
		}
		else {
			this.item.cached();

			node = findNode( path, false );
		}

		if( node == null ) {
			throw new ConfigNotFoundException( path );
		}

		return node;
	}

	@Override
	public Set<String> getKeys()
	{
		final Set<String> set = new TreeSet<>();

		fillKeys( this, set );

		return set;
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

		switch( this.item.kindNoEval() ) {
			case NULL:
				sb.append( "<NULL>" );
			break;

			case ITEM:
			case LINK:
				sb.append( this.item.value().toString() );
			break;

			case NODE:
				sb.append( tree( false ).entrySet().stream().map( Object::toString ).collect( joining( ", ", "{", "}" ) ) );
			break;
		}

		return sb.toString();
	}

	String getExpression()
	{
		switch( this.item.kindNoEval() ) {
			case ITEM:
			case LINK:
				return this.item.value().toString();

			default:
				return null;
		}
	}

	CachedItem item()
	{
		return this.item;
	}

	Map<String, ConfigNodeImpl> tree( boolean create )
	{
		final ConfigNode.Kind kind;

		if( create ) {
			if( this.item.kindNoEval() != Kind.NODE ) {
				this.item = new CachedItem( new TreeMap<>(), this );
			}

			kind = this.item.kindNoEval();
		}
		else {
			kind = this.item.kindNoEval();
		}

		switch( kind ) {
			case NODE:
			case LINK:
				return this.item.cached();

			default:
				return emptyMap();
		}
	}

	void set( String path, Object value )
	{
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

		final Object oldValue = this.item.value();

		if( value != null ) {
			if( this.item.kindNoEval() == Kind.NODE ) {
				throw new ConfigException( format( "Path: %s, cannot change value from NODE to ITEM", this.path ) );
			}

			this.item = new CachedItem( ExpressionOLD.compile( value.toString() ), this );
		}
		else {
			this.item = new CachedItem( this );
		}

		this.pcs.firePropertyChange( this.path, oldValue, this.item.value() );
	}

	ConfigNodeImpl findNode( String path, boolean create )
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
}
