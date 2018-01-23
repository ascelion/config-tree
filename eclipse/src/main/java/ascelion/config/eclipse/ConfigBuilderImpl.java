
package ascelion.config.eclipse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import ascelion.config.impl.ConfigLoad;
import ascelion.config.impl.ConfigScanner;
import ascelion.config.impl.ConfigSourceLiteral;
import ascelion.config.read.ENVConfigReader;
import ascelion.config.read.PRPConfigReader;
import ascelion.config.read.SYSConfigReader;

import static java.util.Arrays.asList;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.eclipse.microprofile.config.spi.Converter;

public class ConfigBuilderImpl implements ConfigBuilder
{

	static private final References<ConfigScanner> SCANNERS = new References<>();

	private final ConfigLoad ld = new ConfigLoad();
	private boolean discoverSources;
	private boolean discoverConverters;
	private ClassLoader cld;
	private final Collection<ConfigSource> sources = new ArrayList<>();
	private final Map<Type, ConverterInfo<?>> converters = new HashMap<>();

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
			addConverter( ConverterInfo.typeOf( c.getClass() ), ConverterInfo.getPriority( c.getClass() ), c );
		}

		return this;
	}

	@Override
	public <T> ConfigBuilder withConverter( Class<T> type, int priority, Converter<T> converter )
	{
		addConverter( type, priority, converter );

		return this;
	}

	@Override
	public Config build()
	{
		final ConfigImpl cf = new ConfigImpl();

		if( this.discoverSources ) {
			cf.addSources( ServiceLoader.load( ConfigSource.class, this.cld ) );
			ServiceLoader.load( ConfigSourceProvider.class, this.cld )
				.forEach( csp -> cf.addSources( csp.getConfigSources( this.cld ) ) );
		}

		if( this.discoverConverters ) {
			cf.addConverters( ServiceLoader.load( Converter.class, this.cld ) );
		}

		cf.addSources( this.sources );
		cf.addConverters( this.converters.values() );

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
