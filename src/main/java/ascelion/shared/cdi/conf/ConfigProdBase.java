
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

	protected <T> T getProperty( InjectionPoint ip )
	{
		final ConfigValue ano = getAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		Object val = this.cc.store().getValue( vec[0] );

		if( val == null && vec.length > 1 ) {
			val = vec[1];
		}
		if( val instanceof String ) {
			final Expander exp = new Expander( ( (String) val ).trim(), x -> (String) this.cc.store().getValue( x ) );

			val = exp.expand();
		}

		return (T) val;
	}

}
