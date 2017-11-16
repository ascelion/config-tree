
package ascelion.config.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigNotFoundException;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;

import static java.util.stream.Collectors.toSet;

public final class ConfigJava
{

	private final ConfigScanner sc = new ConfigScanner();
	private final ConfigLoad ld = new ConfigLoad();
	private final Converters cvs = new Converters();
	private final List<Predicate<ConfigSource>> filters = new ArrayList<>();

	private ConfigNode root;

	public void add( ConfigReader rd )
	{
		this.ld.addReader( rd );
	}

	public void add( ConfigConverter<?> cv )
	{
		this.cvs.register( cv );
	}

	public void add( Type type, ConfigConverter<?> cv )
	{
		this.cvs.register( type, cv );
	}

	public void add( Predicate<ConfigSource> filter )
	{
		this.filters.add( filter );
	}

	public Set<ConfigSource> getSources()
	{
		if( this.filters.isEmpty() ) {
			return this.sc.getSources();
		}
		else {
			return this.sc.getSources().stream().filter( this::accept ).collect( toSet() );
		}
	}

	public ConfigNode root()
	{
		if( this.root == null ) {
			ServiceLoader.load( ConfigReader.class )
				.forEach( this.ld::addReader );
			ServiceLoader.load( ConfigConverter.class )
				.forEach( this.cvs::register );

			this.root = this.ld.load( getSources() );
		}

		return this.root;
	}

	public <T> T getValue( Type type, String prop )
	{
		if( type instanceof ParameterizedType ) {
			final ParameterizedType pt = (ParameterizedType) type;
			final Type raw = pt.getRawType();

			if( raw.equals( Map.class ) ) {
				return (T) getMap( pt.getActualTypeArguments()[0], prop, 0 );
			}
		}

		return (T) this.cvs.create( type, root().getNode( prop ).getValue() );
	}

	public <T> Map<String, T> getMap( Type type, String prop, int unwrap )
	{
		ConfigNode node = root().getNode( prop );

		try {
			final String v = node.getValue();

			if( v != null ) {
				node = root().getNode( v );
			}
		}
		catch( final ConfigNotFoundException e ) {
			;
		}

		final Map<String, T> m = new TreeMap<>();

		node.asMap()
			.forEach( ( k, v ) -> m.put( k, (T) this.cvs.create( type, v ) ) );

		return m;
	}

	private boolean accept( ConfigSource src )
	{
		if( this.filters.isEmpty() ) {
			return true;
		}

		return this.filters.stream().anyMatch( p -> p.test( src ) );
	}
}
