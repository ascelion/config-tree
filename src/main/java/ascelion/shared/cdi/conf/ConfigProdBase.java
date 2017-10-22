
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class ConfigProdBase
{

	@Inject
	ConfigCollect cc;

	protected final ConfigNode getConfigNode( InjectionPoint ip )
	{
		return configNode( ip, annotation( ip ) );
	}

	protected final String getConfigItem( InjectionPoint ip )
	{
		return configItem( ip, annotation( ip ) );
	}

	protected final String expandValue( String value )
	{
		return new Expander( value, x -> {
			return this.cc.getRoot().getItem( x );
		} ).expand();
	}

	protected final String[] expandValues( String value )
	{
		value = new Expander( value, x -> {
			return this.cc.getRoot().getItem( x );
		} ).expand();

		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}

	final ConfigNode configNode( InjectionPoint ip, final ConfigValue ano )
	{
		final String[] vec = ano.value().split( ":" );
		final String path = vec[0];
		ConfigNode cn = this.cc.getRoot().getNode( path );

		if( cn == null && vec.length > 1 ) {
			cn = vec[1] != null ? new ConfigNode( path ).set( vec[1] ) : null;
		}
		if( cn == null ) {
			return null;
		}

		return cn;
	}

	final String configItem( InjectionPoint ip, ConfigValue ano )
	{
		final String[] vec = ano.value().split( ":" );
		String cn = this.cc.getRoot().getItem( vec[0] );

		if( cn == null && vec.length > 1 ) {
			cn = vec[1];
		}

		return expandValue( cn );
	}

	final ConfigValue annotation( InjectionPoint ip )
	{
		return ip.getAnnotated().getAnnotation( ConfigValue.class );
	}

}
