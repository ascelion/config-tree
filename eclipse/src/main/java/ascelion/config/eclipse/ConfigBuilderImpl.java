
package ascelion.config.eclipse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import ascelion.config.eclipse.cs.ENVConfigSource;
import ascelion.config.eclipse.cs.PRPConfigSourceProvider;
import ascelion.config.eclipse.cs.SYSConfigSource;

import static java.util.Arrays.asList;

import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.eclipse.microprofile.config.spi.Converter;

public class ConfigBuilderImpl implements ConfigBuilder
{

	private boolean defaultSources;
	private boolean discoverSources;
	private boolean discoverConverters;
	private ClassLoader cld = getClass().getClassLoader();
	private final Collection<ConfigSource> sources = new ArrayList<>();
	private final Map<Type, ConverterInfo<?>> converters = new HashMap<>();

	@Override
	public ConfigBuilderImpl addDefaultSources()
	{
		this.defaultSources = true;

		return this;
	}

	@Override
	public ConfigBuilderImpl addDiscoveredSources()
	{
		this.discoverSources = true;

		return this;
	}

	@Override
	public ConfigBuilderImpl addDiscoveredConverters()
	{
		this.discoverConverters = true;

		return this;
	}

	@Override
	public ConfigBuilderImpl forClassLoader( ClassLoader cld )
	{
		if( cld != null ) {
			this.cld = cld;
		}

		return this;
	}

	@Override
	public ConfigBuilderImpl withSources( ConfigSource... sources )
	{
		this.sources.addAll( asList( sources ) );

		return this;
	}

	@Override
	public ConfigBuilderImpl withConverters( Converter<?>... converters )
	{
		for( final Converter<?> c : converters ) {
			addConverter( ConverterInfo.typeOf( c.getClass() ), ConverterInfo.getPriority( c.getClass() ), c );
		}

		return this;
	}

	@Override
	public <T> ConfigBuilderImpl withConverter( Class<T> type, int priority, Converter<T> converter )
	{
		addConverter( type, priority, converter );

		return this;
	}

	@Override
	public ConfigImpl build()
	{
		final ConfigImpl cf = new ConfigImpl();

		cf.addSources( this.sources );
		cf.addConverters( this.converters.values() );

		if( this.defaultSources ) {
			cf.addSource( new SYSConfigSource() );
			cf.addSource( new ENVConfigSource() );
			cf.addSources( new PRPConfigSourceProvider().getConfigSources( this.cld ) );
		}
		if( this.discoverSources ) {
			cf.addSources( ServiceLoader.load( ConfigSource.class, this.cld ) );
			ServiceLoader.load( ConfigSourceProvider.class, this.cld )
				.forEach( csp -> cf.addSources( csp.getConfigSources( this.cld ) ) );
		}

		if( this.discoverConverters ) {
			cf.addConverters( ServiceLoader.load( Converter.class, this.cld ) );
		}

		return cf;
	}

	private <T> void addConverter( Type type, int priority, Converter<T> converter )
	{
		this.converters.compute( type, ( t, i ) -> {
			if( i == null || i.priority < priority ) {
				return new ConverterInfo<>( converter, priority );
			}
			else {
				return i;
			}
		} );
	}
}
