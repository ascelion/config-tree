
package ascelion.config.eclipse.cdi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.InjectionPoint;

import ascelion.config.eclipse.ConfigBuilderImpl;
import ascelion.config.eclipse.ConfigImpl;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Typed
@ApplicationScoped
public class ConfigPropertyFactory
{

	private ConfigImpl config;

	@Typed
	@ConfigProperty
	Object getValue( InjectionPoint ip )
	{
		final ConfigProperty cp = ip.getAnnotated().getAnnotation( ConfigProperty.class );
		final String pn = cp.name().isEmpty() ? ip.getMember().getName() : cp.name();
		String pv = this.config.getValue( pn );

		if( pv == null ) {
			if( !ConfigProperty.UNCONFIGURED_VALUE.equals( cp.defaultValue() ) ) {
				pv = cp.defaultValue();
			}
		}

		return this.config.convert( pv, ip.getType() );
	}

	@PostConstruct
	private void postConstruct()
	{
		this.config = new ConfigBuilderImpl()
			.addDefaultSources()
			.addDiscoveredConverters()
			.addDiscoveredSources()
			.build();
	}
}
