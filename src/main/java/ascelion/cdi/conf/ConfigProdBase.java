
package ascelion.cdi.conf;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import ascelion.shared.cdi.conf.ConfigNode;
import ascelion.shared.cdi.conf.ConfigValue;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class ConfigProdBase
{

	@Inject
	ConfigCollect cc;

	protected final ConfigNode getConfigNode( InjectionPoint ip )
	{
		return configNode( annotation( ip ) );
	}

	protected final String getConfigItem( InjectionPoint ip )
	{
		return configItem( ip, annotation( ip ) );
	}

	protected final String expandValue( String value )
	{
		return Eval.eval( value, this.cc.root() );
	}

	protected final String[] splitValues( String value )
	{
		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}

	final ConfigNodeImpl configNode( ConfigValue ano )
	{
		final String[] vec = ano.value().split( ":" );
		final String path = vec[0];
		ConfigNodeImpl cn = this.cc.root().getNode( path );

		if( cn == null && vec.length > 1 && isNotBlank( vec[1] ) ) {
			cn = new ConfigNodeImpl( path );

			cn.setValue( vec[1] );
		}
		if( cn == null ) {
			return null;
		}

		return cn;
	}

	final String configItem( InjectionPoint ip, ConfigValue ano )
	{
		return expandValue( ano.value() );
	}

	final ConfigValue annotation( InjectionPoint ip )
	{
		return ip.getAnnotated().getAnnotation( ConfigValue.class );
	}

}