
package ascelion.config.eclipse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

import ascelion.config.eclipse.cs.ENVConfigSource;
import ascelion.config.eclipse.cs.PRPConfigSourceProvider;
import ascelion.config.eclipse.cs.SYSConfigSource;

import static java.util.Arrays.asList;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.eclipse.microprofile.config.spi.Converter;

final class ConfigBuilderImpl implements ConfigBuilder
{

	private final Collection<ConfigSource> sources = new ArrayList<>();
	private final Collection<ConverterInfo<?>> converters = new ArrayList<>();

	private boolean defaultSources;
	private boolean discoverSources;
	private boolean discoverConverters;
	private ClassLoader cld;

	@Override
	public ConfigBuilder addDefaultSources()
	{
		this.defaultSources = true;

		return this;
	}

	@Override
	public ConfigBuilder addDiscoveredSources()
	{
		this.discoverSources = true;

		return this;
	}

	@Override
	public ConfigBuilder addDiscoveredConverters()
	{
		this.discoverConverters = true;

		return this;
	}

	@Override
	public ConfigBuilder forClassLoader( ClassLoader cld )
	{
		this.cld = cld;

		return this;
	}

	@Override
	public ConfigBuilder withSources( ConfigSource... sources )
	{
		this.sources.addAll( asList( sources ) );

		return this;
	}

	@Override
	public ConfigBuilder withConverters( Converter<?>... converters )
	{
		for( final Converter<?> c : converters ) {
			this.converters.add( new ConverterInfo<>( c ) );
		}

		return this;
	}

	@Override
	public <T> ConfigBuilderImpl withConverter( Class<T> type, int priority, Converter<T> converter )
	{
		this.converters.add( new ConverterInfo<>( converter, priority, type ) );

		return this;
	}

	@Override
	public Config build()
	{
		final ClassLoader cld = ConfigProviderResolver.classLoader( this.cld );
		final ConverterReg cr = new ConverterReg();

		this.converters.forEach( cr::addConverter );

		if( this.discoverConverters ) {
			cr.discover( cld );
		}

		final ConfigImpl cf = new ConfigImpl( cr.get() );

		cf.addSources( this.sources );

		if( this.defaultSources ) {
			cf.addSource( new ENVConfigSource() );
			cf.addSources( new PRPConfigSourceProvider().getConfigSources( cld ) );
			cf.addSource( new SYSConfigSource() );
		}
		if( this.discoverSources ) {
			cf.addSources( ServiceLoader.load( ConfigSource.class, cld ) );
			ServiceLoader.load( ConfigSourceProvider.class, cld )
				.forEach( csp -> cf.addSources( csp.getConfigSources( cld ) ) );
		}

		return cf;
	}
}
