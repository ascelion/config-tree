
package ascelion.config.eclipse;

import java.util.ArrayList;
import java.util.Collection;

import ascelion.config.impl.ConfigLoad;
import ascelion.config.impl.ConfigSourceLiteral;
import ascelion.config.read.ENVConfigReader;
import ascelion.config.read.PRPConfigReader;
import ascelion.config.read.SYSConfigReader;

import static java.util.Arrays.asList;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

public class ConfigBuilderImpl implements ConfigBuilder
{

	private final ConfigLoad ld = new ConfigLoad();
	private boolean discoveredSources;
	private boolean descoveredConverters;
	private ClassLoader cld;
	private final Collection<ConfigSource> sources = new ArrayList<>();
	private final Collection<Converter<?>> converters = new ArrayList<>();

	@Override
	public ConfigBuilder addDefaultSources()
	{
		this.ld.addReader( new PRPConfigReader() );
		this.ld.addReader( new ENVConfigReader() );
		this.ld.addReader( new SYSConfigReader() );

		this.ld.addSource( new ConfigSourceLiteral( "META-INF/microprofile-config.properties", 100, PRPConfigReader.TYPE ) );
		this.ld.addSource( new ConfigSourceLiteral( "", 300, ENVConfigReader.TYPE ) );
		this.ld.addSource( new ConfigSourceLiteral( "", 400, SYSConfigReader.TYPE ) );

		return this;
	}

	@Override
	public ConfigBuilder addDiscoveredSources()
	{
		this.discoveredSources = true;

		return this;
	}

	@Override
	public ConfigBuilder addDiscoveredConverters()
	{
		this.descoveredConverters = true;

		return this;
	}

	@Override
	public ConfigBuilder forClassLoader( ClassLoader loader )
	{
		this.cld = loader;

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
		this.converters.addAll( asList( converters ) );

		return this;
	}

	@Override
	public Config build()
	{
		return new ConfigImpl( this.ld.load() );
	}
}
