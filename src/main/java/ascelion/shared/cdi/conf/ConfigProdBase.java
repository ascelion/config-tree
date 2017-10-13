
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.util.BeanUtils;

public abstract class ConfigProdBase
{

	protected String getProperty( InjectionPoint ip )
	{
		final ConfigValue ano = BeanUtils.extractAnnotation( ip.getAnnotated(), ConfigValue.class );
		final String[] vec = ano.value().split( ":" );
		final String val = ConfigResolver.getPropertyValue( vec[0], vec.length > 1 ? vec[1] : null );

		return val != null ? val.trim() : null;
	}

}
