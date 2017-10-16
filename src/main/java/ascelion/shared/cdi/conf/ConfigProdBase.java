
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public abstract class ConfigProdBase
{

	@Inject
	private ConfigCollect cc;

	protected final ConfigValue getAnnotation( InjectionPoint ip )
	{
		return ip.getAnnotated().getAnnotation( ConfigValue.class );
	}

	protected String getProperty( InjectionPoint ip )
	{
		final ConfigValue ano = getAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		String val = (String) this.cc.cm().getValue( vec[0] );

		if( val == null && vec.length > 1 ) {
			val = vec[1];
		}

		return val != null ? val.trim() : null;
	}

}
