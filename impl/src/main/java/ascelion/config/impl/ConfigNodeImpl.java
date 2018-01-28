
package ascelion.config.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.utils.Expression;

import static ascelion.config.utils.Utils.path;
import static ascelion.config.utils.Utils.pathNames;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class ConfigNodeImpl implements ConfigNode
{

	static class ConfigNodeTA extends TypeAdapter<ConfigNodeImpl>
	{

		@Override
		public void write( JsonWriter out, ConfigNodeImpl node ) throws IOException
		{
			final String expression = node.expression.getExpression();
			final Map<String, ConfigNodeImpl> tree = node.nodes;

			out.beginObject();
			if( expression != null ) {
				out.name( "expression" ).value( expression );
			}
			if( tree != null ) {
				for( final ConfigNode child : tree.values() ) {
					out.name( child.getName() );
					write( out, (ConfigNodeImpl) child );
				}
			}
			out.endObject();
		}

		@Override
		public ConfigNodeImpl read( JsonReader in ) throws IOException
		{
			throw new UnsupportedOperationException();
		}
	}

	private final ConfigNodeImpl root;
	private final String name;
	private final String path;
	private final Expression expression;
	private Map<String, ConfigNodeImpl> nodes;

	public ConfigNodeImpl()
	{
		this.root = this;
		this.name = null;
		this.path = null;
		this.expression = new Expression( this::lookup );
	}

	private ConfigNodeImpl( ConfigNodeImpl parent, String name )
	{
		this.name = name;
		this.path = path( path( parent ), name );
		this.root = parent.root;
		this.expression = new Expression( this.root::lookup );

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
	public String getValue()
	{
		return this.expression.getValue();
	}

	@Override
	public String getRawValue()
	{
		return this.expression.getExpression();
	}

	@Override
	public Collection<ConfigNode> getNodes()
	{
		if( this.nodes != null ) {
			return unmodifiableCollection( this.nodes.values() );
		}
		if( this.expression.isEmpty() ) {
			return null;
		}

		String path = this.expression.getValue();

		if( !this.expression.isChanged() ) {
			return null;
		}

		if( isEmpty( path ) ) {
			path = this.expression.getLastVariable();
		}

		try {
			return this.root.getNode( path ).getNodes();
		}
		catch( final ConfigNotFoundException e ) {
			return null;
		}
	}

	@Override
	public String getValue( String path )
	{
		final Expression expr = new Expression( this.root::lookup, path );
		final ConfigNodeImpl node = findNode( expr.getValue(), false );

		if( node != null ) {
			return node.getValue();
		}

		if( expr.isChanged() ) {
			return expr.getValue();
		}

		throw new ConfigNotFoundException( path );
	}

	@Override
	public ConfigNode getNode( final String path )
	{
		final Expression expr = new Expression( this.root::lookup, path );
		String eval = expr.getValue();

		if( expr.isChanged() && isEmpty( eval ) ) {
			eval = expr.getLastVariable();
		}

		final ConfigNodeImpl node = findNode( eval, false );

		if( node == null ) {
			if( this.expression.isEmpty() ) {
				throw new ConfigNotFoundException( path );
			}

			eval = this.expression.getValue();

			if( this.expression.isChanged() && isEmpty( eval ) ) {
				eval = this.expression.getLastVariable();
			}

			return this.root.getNode( eval ).getNode( path );
		}

		return node;
	}

	@Override
	public Set<String> getKeys()
	{
		final Set<String> keys = new TreeSet<>();

		getKeys( this, keys );

		return unmodifiableSet( keys );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		sb.append( "path: " ).append( this.path );

		if( this.expression != null ) {
			sb.append( ", item: " ).append( this.expression );
		}
		if( this.nodes != null && this.nodes.size() > 0 ) {
			sb.append( ", tree: " )
				.append( this.nodes.entrySet().stream().map( Objects::toString ).collect( joining( ", ", "{", "}" ) ) );
		}

		return sb.toString();
	}

	void setValue( String path, Object value )
	{
		findNode( path, true ).setValue( value );
	}

	void setValue( Object value )
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
		final String oldValue = this.expression.getExpression();

		if( !Objects.equals( oldValue, newValue ) ) {
			this.expression.setExpression( newValue );
		}
	}

	private void getKeys( ConfigNodeImpl node, Set<String> keys )
	{
		if( !node.expression.isEmpty() ) {
			keys.add( node.path );
		}

		if( node.nodes != null ) {
			node.nodes.values().forEach( c -> getKeys( c, keys ) );
		}
	}

	private ConfigNodeImpl findNode( String path, boolean create )
	{
		final String[] keys = pathNames( path );
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
			if( this.nodes == null ) {
				this.nodes = new TreeMap<>();
			}

			return this.nodes.computeIfAbsent( name, x -> new ConfigNodeImpl( this, name ) );
		}
		else {
			return this.nodes != null ? this.nodes.get( name ) : null;
		}
	}

	private Expression.Result lookup( String path )
	{
		final ConfigNode node = findNode( path, false );

		return node != null ? new Expression.Result( node.getRawValue() ) : new Expression.Result();
	}
}
