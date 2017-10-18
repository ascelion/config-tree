
package ascelion.shared.cdi.conf;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

final class ConfigItemImpl implements ConfigItem
{

	static Map<String, ConfigItemImpl> remap( Map<String, ?> map )
	{
		return map.entrySet().stream()
			.filter( e -> e.getValue() != null )
			.collect( Collectors.toMap( e -> e.getKey(), e -> toItem( e.getKey(), e.getValue() ) ) );
	}

	static ConfigItemImpl toItem( String name, Object value )
	{
		if( value instanceof ConfigItemImpl ) {
			return (ConfigItemImpl) value;
		}

		final ConfigItemImpl ci = new ConfigItemImpl( "" );

		if( value instanceof Map ) {
			return ci.set( remap( (Map) value ) );
		}
		if( value instanceof ConfigItem ) {
			return ci.set( (ConfigItem) value );
		}
		if( value != null ) {
			if( value instanceof Collection ) {
				final Collection<?> c = (Collection<?>) value;

				return ci.set( c.stream().map( Object::toString ).collect( Collectors.joining( "," ) ) );
			}
			if( value instanceof Map ) {
				return ci.set( remap( (Map<String, ?>) value ) );
			}
			if( value instanceof Object[] ) {
				final Object[] v = (Object[]) value;

				return ci.set( Stream.of( v ).map( Object::toString ).collect( Collectors.joining( "," ) ) );
			}

			return ci.set( value.toString() );
		}

		return ci;
	}

	private final String name;

	private Kind kind = Kind.TREE;

	private Map<String, ConfigItemImpl> tree;
	private String item;

	ConfigItemImpl( String name )
	{
		this.name = name;
		this.tree = new TreeMap<>();
	}

	@Override
	public String toString()
	{
		final Object v = getValue();

		return String.valueOf( v );
	}

	@Override
	public Kind getKind()
	{
		return this.kind;
	}

	@Override
	public String getItem()
	{
		return this.kind == Kind.ITEM ? this.item : null;
	}

	@Override
	public Map<String, ConfigItemImpl> getTree()
	{
		return this.kind == Kind.TREE ? unmodifiableMap( this.tree ) : null;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public <T> Map<String, T> asMap( Function<String, T> fun )
	{
		switch( this.kind ) {
			case ITEM:
				return singletonMap( this.name, fun.apply( this.item ) );

			case TREE:
				final Map<String, T> m = new TreeMap<>();

				fillTree( this.tree, "", m, fun );

				return unmodifiableMap( m );

			default:
				return null;
		}
	}

	private <T> void fillTree( Map<String, ConfigItemImpl> tree, String prefix, Map<String, T> map, Function<String, T> fun )
	{
		tree.forEach( ( k, v ) -> {
			k = isNotBlank( prefix ) ? format( "%s.%s", prefix, k ) : k;

			switch( v.getKind() ) {
				case ITEM:
					map.put( k, fun.apply( v.getItem() ) );
				break;

				case TREE:
					fillTree( v.getTree(), k, map, fun );

				default:
			}
		} );
	}

	Map<String, ConfigItemImpl> tree()
	{
		if( this.tree == null ) {
			set( new TreeMap<>() );
		}

		return this.tree;
	}

	ConfigItemImpl add( ConfigItem ci )
	{
		switch( ci.getKind() ) {
			case ITEM:
				set( ci.getItem() );
			break;

			case TREE:
				add( ci.getTree() );

			default:
		}

		return this;
	}

	ConfigItemImpl add( Map<String, ? extends ConfigItem> tree )
	{
		this.kind = Kind.TREE;
		this.item = null;

		if( this.tree == null ) {
			this.tree = new TreeMap<>();
		}

		tree.entrySet().stream()
			.filter( e -> Objects.nonNull( e.getValue() ) && e.getValue().getKind() != Kind.NONE )
			.forEach( e -> {
				final String k = e.getKey();
				final ConfigItem s = e.getValue();
				final ConfigItemImpl t = tree().computeIfAbsent( k, ConfigItemImpl::new );

				t.add( s );
			} );

		return this;
	}

	ConfigItemImpl set( ConfigItem ci )
	{
		this.kind = ci.getKind();
		this.item = ci.getItem();
		this.tree = ci.getTree() != null ? remap( ci.getTree() ) : null;

		return this;
	}

	ConfigItemImpl set( String item )
	{
		this.kind = Kind.ITEM;
		this.item = item;
		this.tree = null;

		return this;
	}

	ConfigItemImpl set( Map<String, ? extends ConfigItem> tree )
	{
		this.kind = Kind.TREE;
		this.item = null;
		this.tree = remap( tree );

		return this;
	}
}
