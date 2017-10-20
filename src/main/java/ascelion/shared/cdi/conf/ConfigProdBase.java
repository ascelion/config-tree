
package ascelion.shared.cdi.conf;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class ConfigProdBase
{

	@Inject
	protected ConfigCollect cc;

	protected final ConfigValue getAnnotation( InjectionPoint ip )
	{
		return ip.getAnnotated().getAnnotation( ConfigValue.class );
	}

	protected ConfigNode getConfig( InjectionPoint ip )
	{
		final ConfigValue ano = getAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		ConfigNode cn = this.cc.getRoot().getNode( vec[0] );

		if( cn == null && vec.length > 1 ) {
			cn = vec[1] != null ? new ConfigNode( vec[0] ).set( vec[1] ) : null;
		}
		if( cn == null ) {
			return null;
		}

		return cn;
	}

	protected String getConfigValue( InjectionPoint ip )
	{
		final ConfigValue ano = getAnnotation( ip );
		final String[] vec = ano.value().split( ":" );
		String cn = this.cc.getRoot().getItem( vec[0] );

		if( cn == null && vec.length > 1 ) {
			cn = vec[1];
		}

		return expandValue( cn );
	}

	protected String expandValue( String value )
	{
		return new Expander( value, x -> {
			return this.cc.getRoot().getItem( x );
		} ).expand();
	}

	protected String[] expandValues( String value )
	{
		value = new Expander( value, x -> {
			return this.cc.getRoot().getItem( x );
		} ).expand();

		return isNotBlank( value ) ? value.split( "\\s*[;,]\\s*" ) : new String[0];
	}
}
