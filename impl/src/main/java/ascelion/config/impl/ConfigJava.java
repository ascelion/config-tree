
package ascelion.config.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;

import ascelion.config.api.ConfigConverter;
import ascelion.config.api.ConfigNode;
import ascelion.config.api.ConfigReader;
import ascelion.config.api.ConfigSource;
import ascelion.config.conv.Converters;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class ConfigJava
{

	private final ConfigScanner sc = new ConfigScanner();
	private final ConfigLoad ld = new ConfigLoad();
	private final Converters cvs = new Converters();
	private final List<Predicate<ConfigSource>> filters = new ArrayList<>();

	private ConfigNode root;
	{
		this.cvs.setRootNode( this::root );
	}

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

	public void addFilter( Predicate<ConfigSource> filter )
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

	public Collection<ConfigReader> getReaders()
	{
		return this.sc.getReaders().stream()
			.map( this::create )
			.filter( Objects::nonNull )
			.collect( toList() );
	}

	public ConfigNode root()
	{
		if( this.root == null ) {
			ServiceLoader.load( ConfigConverter.class )
				.forEach( this.cvs::register );

			this.ld.addReaders( getReaders() );
			this.ld.addSources( getSources() );

			this.root = this.ld.load();
		}

		return this.root;
	}

	public Converters getConverter()
	{
		return this.cvs;
	}

	private ConfigReader create( Class<? extends ConfigReader> t )
	{
		try {
			return t.newInstance();
		}
		catch( InstantiationException | IllegalAccessException e ) {
			return null;
		}
	}

	private boolean accept( ConfigSource src )
	{
		if( this.filters.isEmpty() ) {
			return true;
		}

		return this.filters.stream().anyMatch( p -> p.test( src ) );
	}
}
